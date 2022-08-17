/*
 * Tentackle - https://tentackle.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.tentackle.bundlemonkey.common;

import org.tentackle.app.Application;
import org.tentackle.i18n.pdo.StoredBundle;
import org.tentackle.i18n.pdo.StoredBundleKey;
import org.tentackle.pdo.DomainContext;
import org.tentackle.pdo.DomainContextProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

/**
 * The bundle repository encapsulates the StoredBundle and StoredBundleKey persistence.<br>
 * It maps the i18n PDOs to internal DTOs. This allows the other modules to refer to the DTOs
 * only to avoid dependencies on the persistence layer.
 */
public class BundleRepository implements DomainContextProvider {

  private final DomainContext context;                      // domain context to load from storage
  private final List<BundlesUpdatedListener> listeners;     // translation modification listener
  private final Preferences prefs;                          // user preferences for the active locales
  private final Set<String> activeLocales;                  // the selected locales for the current user
  private final Set<String> locales;                        // all locales persisted in the database (including the default "")
  private final Map<String, BundleName> bundleNames;        // internal bundle model, null to force (re)load
  private boolean invalid;                                  // true if repo is outdated and needs to be reloaded
  private int modificationCount;                            // number of modified translations
  private int missingCount;                                 // -1 if pending computation

  public BundleRepository() {
    context = Application.getInstance().getDomainContext();
    listeners = new ArrayList<>();
    bundleNames = new TreeMap<>();
    prefs = Preferences.userNodeForPackage(getClass());
    activeLocales = new TreeSet<>();
    locales = new TreeSet<>();
    invalid = true;
  }

  @Override
  public DomainContext getDomainContext() {
    return context;
  }

  /**
   * Returns all locales.<br>
   * The default locale is the empty string.
   *
   * @return the locales
   */
  public Set<String> getLocales() {
    assertLoaded();
    return locales;
  }

  /**
   * Returns all active locales.
   *
   * @return the active locales
   */
  public Set<String> getActiveLocales() {
    assertLoaded();
    return activeLocales;
  }

  /**
   * Returns whether the given locale is active.
   *
   * @param locale the locale
   * @return true if active
   */
  public boolean isLocaleActive(String locale) {
    assertLoaded();
    return activeLocales.contains(locale);
  }

  /**
   * Sets a locale active / deactivated.
   *
   * @param locale the locale
   * @param active true if active, else deactivate
   */
  public void setLocaleActive(String locale, boolean active) {
    if (isLocaleActive(locale) != active) {
      if (active) {
        activeLocales.add(locale);
      }
      else {
        activeLocales.remove(locale);
      }
      prefs.putBoolean(locale, active);
      invalid = true;
      missingCount = -1;
      for (BundleName bundleName : bundleNames.values()) {
        for (BundleKey bundleKey : bundleName.getKeys().values()) {
          bundleKey.invalidateMissingCount();
        }
      }
      assertLoaded();
    }
  }

  /**
   * Gets the loaded bundle nodes.
   *
   * @return the bundles
   */
  public Collection<? extends BundleNode> getBundles() {
    assertLoaded();
    return bundleNames.values();
  }

  /**
   * Gets of modifications made so far.
   *
   * @return the modification count
   */
  public int getModificationCount() {
    assertLoaded();
    return modificationCount;
  }

  /**
   * Gets the number of missing translations.
   *
   * @return the missing count
   */
  public int getMissingCount() {
    assertLoaded();
    if (missingCount == -1) {
      missingCount = 0;
      for (BundleName bundleName : bundleNames.values()) {
        missingCount += bundleName.getMissingCount();
      }
    }
    return missingCount;
  }

  /**
   * Adds a bundle update listener.
   *
   * @param listener the listener
   */
  public void addBundlesUpdatedListener(BundlesUpdatedListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a bundle update listener.
   *
   * @param listener the listener
   */
  public void removeBundlesUpdatedListener(BundlesUpdatedListener listener) {
    listeners.remove(listener);
  }

  /**
   * Updates the modification count.
   *
   * @param diff number of modifications to add
   */
  public void updateModificationCount(int diff) {
    missingCount = -1;
    modificationCount += diff;
    fireListeners(false);
  }

  /**
   * Invalidates the repository.
   */
  public synchronized void invalidate() {
    invalid = true;
  }

  /**
   * Persists the bundles back to storage.
   */
  public void persist() {
    assertLoaded();
    for (BundleName bundleName : bundleNames.values()) {
      if (bundleName.getModificationCount() != 0) {
        for (String locale : activeLocales) {
          persist(bundleName, locale.isEmpty() ? null : locale);
        }
      }
    }
  }

  /**
   * Renames or moves a bundle.
   * <p>
   * Throws {@link org.tentackle.session.ConstraintException} if renaming failed.
   *
   * @param bundleName the bundle
   * @param newName the new name of the bundle
   */
  public void renameBundle(BundleName bundleName, String newName) {
    context.getSession().transaction(() -> {
      for (StoredBundle storedBundle : getStoredBundles(bundleName)) {
        storedBundle.setName(newName);
        storedBundle.persist();
        invalidate();
      }
      return null;
    });
  }

  /**
   * Renames a package.
   * <p>
   * Throws {@link org.tentackle.session.ConstraintException} if renaming failed.
   *
   * @param oldPackage the old package name
   * @param newPackage the new package name
   */
  public void renamePackage(String oldPackage, String newPackage) {
    String packageName;
    if (!newPackage.isEmpty() && !newPackage.endsWith(".")) {
      packageName = newPackage + ".";
    }
    else {
      packageName = newPackage;
    }
    context.getSession().transaction(() -> {
      for (BundleName bundleName : getBundlesForPackage(oldPackage)) {
        for (StoredBundle storedBundle : getStoredBundles(bundleName)) {
          String remainder = storedBundle.getName().substring(oldPackage.length());
          if (remainder.startsWith(".")) {
            remainder = remainder.substring(1);
          }
          storedBundle.setName(packageName + remainder);
          storedBundle.persist();
          invalidate();
        }
      }
      return null;
    });
  }


  /**
   * Persiste the given bundle for a locale.
   *
   * @param bundleName the bundle
   * @param locale the locale, null if default locale
   */
  private void persist(BundleName bundleName, String locale) {
    context.getSession().transaction(() -> {    // one transaction per bundle
      StoredBundle storedBundle = on(StoredBundle.class).findByNameAndLocale(bundleName.getName(), locale);
      if (storedBundle == null) {
        storedBundle = on(StoredBundle.class);
        storedBundle.setName(bundleName.getName());
        storedBundle.setLocale(locale);
      }
      for (BundleKey bundleKey : bundleName.getKeys().values()) {
        BundleTranslation translation = bundleKey.getTranslation(locale == null ? "" : locale);
        storedBundle.setTranslation(bundleKey.getName(), translation == null ? null : translation.getValue());
      }
      if (storedBundle.isModified()) {
        storedBundle.save();
      }
      return null;
    });
  }

  /**
   * Asserts that the repository is loaded.
   */
  private synchronized void assertLoaded() {
    if (invalid) {
      loadBundlesFromStorage();
      invalid = false;
      fireListeners(true);
    }
  }

  /**
   * Fires the repo listeners.
   *
   * @param reloaded true if repo has been reloaded
   */
  private void fireListeners(boolean reloaded) {
    for (BundlesUpdatedListener listener : listeners) {
      listener.bundlesUpdated(modificationCount, reloaded);
    }
  }

  /**
   * Loads the bundles from the database.
   */
  private void loadBundlesFromStorage() {
    bundleNames.clear();
    locales.clear();

    // load persisted bundles
    for (StoredBundle storedBundle : on(StoredBundle.class).selectAll()) {
      String locale = storedBundle.getLocale();
      if (locale == null) {
        locale = "";
      }
      locales.add(locale);
      boolean active = prefs.getBoolean(locale, true);
      if (active) {
        activeLocales.add(locale);
      }
      else {
        activeLocales.remove(locale);
      }
      BundleName bundleName = bundleNames.computeIfAbsent(storedBundle.getName(), n -> new BundleName(this, n, new TreeMap<>()));
      for (StoredBundleKey storedKey : storedBundle.getKeys()) {
        BundleKey bundleKey = bundleName.getKeys().computeIfAbsent(storedKey.getKey(), k -> new BundleKey(bundleName, k, new TreeMap<>()));
        bundleKey.getTranslations().computeIfAbsent(locale, l -> new BundleTranslation(bundleKey, l, storedKey.getValue(), active));
      }
    }

    // add missing translations (for the active locales only!)
    for (BundleName bundleName : bundleNames.values()) {
      for (BundleKey bundleKey : bundleName.getKeys().values()) {
        for (String locale : activeLocales) {
          BundleTranslation translation = bundleKey.getTranslation(locale);
          if (translation == null) {
            bundleKey.getTranslations().put(locale, new BundleTranslation(bundleKey, locale, null, true));
          }
        }
      }
    }

    modificationCount = 0;
    missingCount = -1;
  }

  /**
   * Returns the stored bundles for the given bundle name.
   *
   * @param bundleName the bundlename
   * @return the stored bundles for all locales
   */
  private List<StoredBundle> getStoredBundles(BundleName bundleName) {
    return on(StoredBundle.class).findByName(bundleName.getName());
  }

  /**
   * Returns the stored bundles for the given bundle name.
   *
   * @param packageName the package name
   * @return the stored bundles for all locales
   */
  private List<BundleName> getBundlesForPackage(String packageName) {
    if (!packageName.endsWith(".")) {
      packageName += ".";
    }
    List<BundleName> bundles = new ArrayList<>();
    for (BundleName bundle : bundleNames.values()) {
      if (bundle.getName().startsWith(packageName)) {
        bundles.add(bundle);
      }
    }
    return bundles;
  }

}
