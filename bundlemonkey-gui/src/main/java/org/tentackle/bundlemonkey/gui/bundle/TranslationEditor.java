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

import org.tentackle.bind.Bindable;
import org.tentackle.bundlemonkey.common.BundleMonkeyPreferences;
import org.tentackle.bundlemonkey.common.BundleTranslation;
import org.tentackle.common.StringHelper;
import org.tentackle.fx.AbstractFxController;
import org.tentackle.fx.FxControllerService;
import org.tentackle.fx.component.FxLabel;
import org.tentackle.fx.component.FxTextArea;

import javafx.fxml.FXML;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Editor for a translation.
 */
@FxControllerService
public class TranslationEditor extends AbstractFxController {

  private static final String STYLE_MODIFIED = "translation-modified";

  @Bindable
  private BundleTranslation translation;

  @FXML
  private FxLabel translationLocale;

  @FXML
  private FxTextArea translationValue;


  @FXML
  private ResourceBundle resources;

  @FXML
  public void initialize() {
    updatePreferredSizes();
    BundleMonkeyPreferences.getInstance().getSystemPrefs().addPreferenceChangeListener(evt -> {
      switch (evt.getKey()) {
        case BundleMonkeyPreferences.TRANSLATION_ROWS:
        case BundleMonkeyPreferences.TRANSLATION_COLUMNS:
          updatePreferredSizes();
          break;
      }
    });
  }

  @Override
  public void configure() {
    translationValue.addViewToModelListener(this::valueUpdated);
  }

  /**
   * Sets a translation for a language.
   *
   * @param translation the translation
   */
  public void setTranslation(BundleTranslation translation) {
    this.translation = translation;
    String locale = translation == null ? "" : translation.getLocale();
    if (StringHelper.isAllWhitespace(locale)) {
      locale = resources.getString("default");
    }
    translationLocale.setText(locale);
    // set initial first to allow Ctrl-Z via saveView below
    translationValue.setText(translation == null ? null : translation.getInitialValue());
    getContainer().saveView();
    getContainer().updateView();   // this sets the current translation
    valueUpdated();
  }

  /**
   * Requests the focus for the translation text area.
   */
  public void requestFocus() {
    translationValue.requestFocus();
  }

  /**
   * Sets the text area editable.
   *
   * @param editable false if locale is not active
   */
  public void setEditable(boolean editable) {
    translationValue.setEditable(editable);
  }

  private void updatePreferredSizes() {
    translationValue.setPrefColumnCount(BundleMonkeyPreferences.getInstance().getTranslationColumns());
    translationValue.setPrefRowCount(BundleMonkeyPreferences.getInstance().getTranslationRows());
  }

  private void valueUpdated() {
    if (translation.getValue() != null && !Objects.equals(translation.getValue(), translation.getInitialValue())) {
      if (!translationValue.getStyleClass().contains(STYLE_MODIFIED)) {
        getView().getStyleClass().add(STYLE_MODIFIED);
      }
    }
    else {
      getView().getStyleClass().remove(STYLE_MODIFIED);
    }
  }
}
