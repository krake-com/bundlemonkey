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

package org.tentackle.bundlemonkey.gui.refactor;

import org.tentackle.bind.Bindable;
import org.tentackle.bundlemonkey.common.BundleName;
import org.tentackle.common.StringHelper;
import org.tentackle.fx.AbstractFxController;
import org.tentackle.fx.Fx;
import org.tentackle.fx.FxControllerService;
import org.tentackle.fx.component.FxButton;
import org.tentackle.fx.component.FxTextField;
import org.tentackle.reflect.ReflectionHelper;
import org.tentackle.session.ConstraintException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Dialog to rename or move a bundle to another package.
 */
@FxControllerService
public class RenameBundleDialog extends AbstractFxController {

  /**
   * Shows the dialog.
   *
   * @param bundle the bundle to rename
   * @param done runnable invoked when dialog is done
   */
  public static void show(BundleName bundle, Runnable done) {
    Stage stage = Fx.createStage(Modality.NONE);
    RenameBundleDialog dialog = Fx.load(RenameBundleDialog.class);
    dialog.bundle = Objects.requireNonNull(bundle);
    dialog.newName = ReflectionHelper.getPackageName(bundle.getName());
    if (!dialog.newName.isEmpty()) {
      dialog.newName += ".";
    }
    stage.setTitle(dialog.resources.getString("rename bundle"));
    Scene scene = Fx.createScene(dialog.getView());
    stage.setScene(scene);
    stage.setOnHidden(event -> {
      if (dialog.done) {
        done.run();
      }
    });
    Fx.show(stage);
    dialog.getContainer().updateView();
    dialog.newNameField.requestFocus();
    dialog.newNameField.positionCaret(dialog.newName.length());
  }

  @Bindable
  private BundleName bundle;

  @Bindable
  private String newName;

  @FXML
  private FxTextField bundleNameField;

  @FXML
  private FxTextField newNameField;

  @FXML
  private FxButton cancelButton;

  @FXML
  private FxButton renameButton;

  @FXML
  private ResourceBundle resources;

  private boolean done;


  @FXML
  private void initialize() {
    cancelButton.setOnAction(event -> getStage().close());
    renameButton.setOnAction(event -> renameBundle());
    newNameField.setOnAction(event -> renameButton.doClick());
  }

  private void renameBundle() {
    if (StringHelper.isValidJavaClassName(newName)) {
      try {
        bundle.getRepository().renameBundle(bundle, newName);
        done = true;
      }
      catch (ConstraintException cx) {
        Fx.error(getView(), MessageFormat.format(resources.getString("bundle {0} already exists"), newName));
      }
    }
    else {
      Fx.error(getView(), MessageFormat.format(resources.getString("not a valid java name: {0}"), newName));
    }
    getStage().close();
  }

}
