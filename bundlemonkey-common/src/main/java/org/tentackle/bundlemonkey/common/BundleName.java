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
import org.tentackle.validate.Validateable;
import org.tentackle.validate.ValidationScopeFactory;
import org.tentackle.validate.ValidationUtilities;
import org.tentackle.validate.validator.NotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BundleName implements BundleNode, Validateable {

  private final BundleRepository repository;
  private final String name;
  private final Map<String, BundleKey> keys;
  private String comment;
  private int modificationCount;
  private int missingCount = -1;    // -1 if pending computation

  public BundleName(BundleRepository repository, String name, Map<String, BundleKey> keys) {
    this.repository = repository;
    this.name = name;
    this.keys = keys;
    validate(ValidationUtilities.getInstance().getDefaultValidationPath(this),
             ValidationScopeFactory.getInstance().getAllScope());
  }

  /**
   * Gets the bundle repository.
   *
   * @return the repository
   */
  public BundleRepository getRepository() {
    return repository;
  }

  @Override
  @NotEmpty
  @Bindable
  public String getName() {
    return name;
  }

  @Override
  public Integer getCount() {
    return keys.size();
  }

  @Override
  public String getComment() {
    return comment;
  }

  public Map<String, BundleKey> getKeys() {
    return keys;
  }

  @Override
  public List<? extends BundleNode> getNodes() {
    return new ArrayList<>(keys.values());
  }

  @Override
  public void updateModificationCount(int diff) {
    missingCount = -1;
    modificationCount += diff;
    repository.updateModificationCount(diff);
  }

  @Override
  public int getModificationCount() {
    return modificationCount;
  }

  @Override
  public int getMissingCount() {
    if (missingCount == -1) {
      missingCount = 0;
      int translationCount = 0;
      for (BundleKey key : keys.values()) {
        missingCount += key.getMissingCount();
        translationCount += key.getTranslationCount();
      }
      comment = CommonBundle.createComment(modificationCount, missingCount, translationCount - missingCount);
    }
    return missingCount;
  }

  @Override
  public void invalidateMissingCount() {
    missingCount = -1;
  }

}
