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

package org.tentackle.bundlemonkey.gui.dto;

import javafx.scene.control.TreeItem;

import org.tentackle.bundlemonkey.common.BundleNode;
import org.tentackle.fx.table.DefaultTableConfigurationProvider;
import org.tentackle.fx.table.TableConfiguration;
import org.tentackle.fx.table.TableConfigurationProviderService;

/**
 * Table configuration provider for bundle nodes.
 */
@TableConfigurationProviderService(BundleNode.class)
public class BundleNodeTableConfigurationProvider extends DefaultTableConfigurationProvider<BundleNode> {

  @Override
  protected TableConfiguration<BundleNode> createTableConfiguration(Class<BundleNode> clazz, String name) {
    return new BundleNodeTableConfiguration<>(clazz, name) {

      @Override
      @SuppressWarnings({ "unchecked", "rawtypes" })
      public TreeItem<BundleNode> createTreeItem(BundleNode object) {
        return new BundleNodeTreeItem(object);
      }
    };
  }

}
