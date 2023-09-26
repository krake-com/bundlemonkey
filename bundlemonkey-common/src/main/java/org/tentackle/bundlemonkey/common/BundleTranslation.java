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

import org.tentackle.bind.Bindable;
import org.tentackle.validate.ScopeConfigurator;
import org.tentackle.validate.ValidationScope;
import org.tentackle.validate.scope.MandatoryScope;
import org.tentackle.validate.validator.NotNull;

import java.util.Objects;

/**
 * Translation for a bundle key and locale.
 */
public class BundleTranslation implements Comparable<BundleTranslation>, ScopeConfigurator {

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static final Class<? extends ValidationScope>[] scopes = new Class[]{MandatoryScope.class};

  private final BundleKey bundleKey;
  private final String locale;
  private final String initialValue;
  private final boolean active;
  private String value;
  private boolean modified;
  private boolean missing;

  /**
   * Creates a translation.
   *
   * @param bundleKey the bundle key this translation belongs to
   * @param locale the locale, empty string if default
   * @param initialValue the initial translation value, null if translation is new (language added, for example)
   * @param active true if belongs to the active locales
   */
  public BundleTranslation(BundleKey bundleKey, String locale, String initialValue, boolean active) {
    this.bundleKey = Objects.requireNonNull(bundleKey);
    this.locale = Objects.requireNonNull(locale);
    this.initialValue = this.value = initialValue;
    this.active = active;
  }

  /**
   * Gets the bundle key.
   *
   * @return the bundle key
   */
  public BundleKey getBundleKey() {
    return bundleKey;
  }

  /**
   * Gets the initial value.
   *
   * @return the initial value
   */
  public String getInitialValue() {
    return initialValue;
  }

  @Bindable
  @NotNull
  public String getLocale() {
    return locale;
  }

  /**
   * Returns whether the translation is active.
   *
   * @return true if belongs to the active locales
   */
  public boolean isActive() {
    return active;
  }

  @Bindable
  @NotNull
  public String getValue() {
    return value;
  }

  @Bindable
  public void setValue(String value) {
    this.value = value;
    boolean modified = !Objects.equals(value, initialValue);
    boolean missing = value == null;
    if (this.modified != modified) {
      this.modified = modified;
      this.missing = missing;
      bundleKey.updateModificationCount(modified ? 1 : -1);
    }
    else if (this.missing != missing) {
      this.missing = missing;
      bundleKey.updateModificationCount(0);   // just trigger update of colors in bundle tree table
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BundleTranslation that = (BundleTranslation) o;

    return locale.equals(that.locale);
  }

  @Override
  public int hashCode() {
    return locale.hashCode();
  }

  @Override
  public int compareTo(BundleTranslation o) {
    return locale.compareTo(o.locale);
  }

  @Override
  public Class<? extends ValidationScope>[] getDefaultScopes() {
    return scopes;
  }

  /**
   * Returns whether the translation was modified.
   *
   * @return true if
   */
  public boolean isModified() {
    return !Objects.equals(initialValue, value);
  }

}
