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

package org.tentackle.bundlemonkey.gui.bundle;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.tentackle.bundlemonkey.common.BundleKey;
import org.tentackle.bundlemonkey.common.BundleNode;
import org.tentackle.bundlemonkey.common.BundleTranslation;
import org.tentackle.fx.AbstractFxController;
import org.tentackle.fx.Fx;
import org.tentackle.fx.FxControllerService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Editor for the bundle keys.
 */
@FxControllerService(url = FxControllerService.FXML_NONE, binding = FxControllerService.BINDING.NO)
public class BundleEditor extends AbstractFxController {

  private final ResourceBundle resources;
  private final ScrollPane scrollPane;
  private final StackPane stackPane;
  private final Label emptyLabel;
  private final VBox propertyPane;
  private final Map<String, TranslationEditor> translationEditors;
  private Set<String> activeLocales;
  private BundleKey bundleKey;

  /**
   * Creates a bundle editor.
   *
   * @param resources the resource bundle
   */
  public BundleEditor(ResourceBundle resources) {
    this.resources = resources;
    translationEditors = new LinkedHashMap<>();
    emptyLabel = Fx.createNode(Label.class);
    emptyLabel.setText(resources.getString("please select a bundle key"));
    propertyPane = Fx.createNode(VBox.class);
    propertyPane.setVisible(false);
    scrollPane = Fx.createNode(ScrollPane.class);
    scrollPane.setFitToWidth(true);
    stackPane = Fx.createNode(StackPane.class);
    stackPane.getChildren().addAll(emptyLabel, propertyPane);
    scrollPane.setContent(stackPane);
    setView(scrollPane);
  }

  /**
   * Sets the locales.
   *
   * @param locales all locales
   * @param activeLocales the active locales
   */
  public void setLocales(Set<String> locales, Set<String> activeLocales) {
    this.activeLocales = activeLocales;
    translationEditors.clear();
    for (String locale : activeLocales) {   // active first
      translationEditors.put(locale, Fx.load(TranslationEditor.class));
    }
    for (String locale : locales) {   // others next
      translationEditors.putIfAbsent(locale, Fx.load(TranslationEditor.class));
    }
    propertyPane.getChildren().clear();
    for (Map.Entry<String, TranslationEditor> entry : translationEditors.entrySet()) {
      String locale = entry.getKey();
      TranslationEditor translationEditor = entry.getValue();
      propertyPane.getChildren().add(translationEditor.getView());
      translationEditor.setEditable(activeLocales.contains(locale));
    }
    if (bundleKey != null) {
      setBundleNode(bundleKey);
    }
  }

  /**
   * Sets the current selected bundle node.
   *
   * @param bundleNode the bundle node
   */
  public void setBundleNode(BundleNode bundleNode) {
    if (bundleNode instanceof BundleKey) {
      bundleKey = (BundleKey) bundleNode;
      emptyLabel.setVisible(false);
      propertyPane.setVisible(true);
      for (Map.Entry<String, TranslationEditor> entry : translationEditors.entrySet()) {
        String locale = entry.getKey();
        TranslationEditor editor = entry.getValue();
        BundleTranslation translation = bundleKey.getTranslation(locale);
        if (translation == null) {
          translation = new BundleTranslation(bundleKey, locale, null, activeLocales != null && activeLocales.contains(locale));
          bundleKey.getTranslations().put(locale, translation);
          bundleKey.invalidateMissingCount();
        }
        editor.setTranslation(translation);
      }
    }
    else {
      bundleKey = null;
      emptyLabel.setVisible(true);
      propertyPane.setVisible(false);
    }
  }

  /**
   * Selects the locale and requests the focus.
   *
   * @param locale the locale
   */
  public void selectLocale(String locale) {
    TranslationEditor translationEditor = translationEditors.get(locale);
    if (translationEditor != null) {
      translationEditor.requestFocus();
    }
  }

}
