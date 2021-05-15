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

import org.tentackle.validate.Validateable;
import org.tentackle.validate.ValidationScopeFactory;
import org.tentackle.validate.ValidationUtilities;
import org.tentackle.validate.validator.NotEmpty;
import org.tentackle.validate.validator.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A bundle key.<br>
 * The key holds the translations for the locales.
 */
public class BundleKey implements BundleNode, Validateable {

  private final BundleName bundleName;
  private final String key;
  private final Map<String, BundleTranslation> translations;
  private int modificationCount;
  private int missingCount = -1;    // -1 if pending computation
  private int translationCount;
  private String comment;

  /**
   * Creates a bundle key.
   *
   * @param bundleName the bundle name (resource bundle)
   * @param key the resource key
   * @param translations the resource translations
   */
  public BundleKey(BundleName bundleName, String key, Map<String, BundleTranslation> translations) {
    this.bundleName = bundleName;
    this.key = key;
    this.translations = translations;
    validate(ValidationUtilities.getInstance().getDefaultValidationPath(this),
             ValidationScopeFactory.getInstance().getAllScope());
  }

  /**
   * Gets the bundle name this key belongs to.
   *
   * @return the bundle name
   */
  public BundleName getBundleName() {
    return bundleName;
  }

  @Override
  @NotEmpty
  public String getName() {
    return key;
  }

  @Override
  public Integer getCount() {
    return translations.size();
  }

  @Override
  public String getComment() {
    return comment;
  }

  @NotNull
  public Map<String, BundleTranslation> getTranslations() {
    return translations;
  }

  /**
   * Gets the translation for a given locale.
   *
   * @param locale the locale, empty string if default locale
   * @return the translation, null if no translation for that locale
   */
  public BundleTranslation getTranslation(String locale) {
    return translations.get(locale);
  }

  @Override
  public List<? extends BundleNode> getNodes() {
    return null;
  }

  @Override
  public void updateModificationCount(int diff) {
    missingCount = -1;
    modificationCount += diff;
    bundleName.updateModificationCount(diff);
  }

  @Override
  public int getModificationCount() {
    return modificationCount;
  }

  @Override
  public void invalidateMissingCount() {
    missingCount = -1;
    bundleName.invalidateMissingCount();
  }

  @Override
  public int getMissingCount() {
    if (missingCount == -1) {
      missingCount = 0;
      translationCount = 0;
      for (BundleTranslation translation : translations.values()) {
        if (translation.isActive()) {
          translationCount++;
        }
        if (translation.getValue() == null) {
          missingCount++;
        }
      }
      comment = CommonBundle.createComment(modificationCount, missingCount, translationCount - missingCount);
    }
    return missingCount;
  }

  /**
   * Gets the number of translations.
   *
   * @return the number of active translations
   */
  public int getTranslationCount() {
    getMissingCount();
    return translationCount;
  }

}
