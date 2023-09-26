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
import org.tentackle.bundlemonkey.common.BundleRepository;
import org.tentackle.common.StringHelper;
import org.tentackle.fx.AbstractFxController;
import org.tentackle.fx.Fx;
import org.tentackle.fx.FxControllerService;
import org.tentackle.fx.component.FxButton;
import org.tentackle.fx.component.FxTextField;
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
public class RenamePackageDialog extends AbstractFxController {

  /**
   * Shows the dialog.
   *
   * @param packageName the old package name
   * @param bundleRepository the bundle repository
   * @param done invoked when dialog is done
   */
  public static void show(String packageName, BundleRepository bundleRepository, Runnable done) {
    Stage stage = Fx.createStage(Modality.NONE);
    RenamePackageDialog dialog = Fx.load(RenamePackageDialog.class);
    dialog.oldName = Objects.requireNonNull(packageName);
    dialog.bundleRepository = bundleRepository;
    stage.setTitle(dialog.resources.getString("rename package"));
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
  }

  @Bindable
  private String oldName;

  @Bindable
  private String newName;

  @FXML
  private FxTextField oldNameField;

  @FXML
  private FxTextField newNameField;

  @FXML
  private FxButton cancelButton;

  @FXML
  private FxButton renameButton;

  @FXML
  private ResourceBundle resources;


  private BundleRepository bundleRepository;

  private boolean done;

  @FXML
  private void initialize() {
    cancelButton.setOnAction(event -> getStage().close());
    renameButton.setOnAction(event -> renameBundle());
    newNameField.setOnAction(event -> renameButton.doClick());
  }

  private void renameBundle() {
    if (StringHelper.isValidJavaPackageName(newName)) {
      try {
        bundleRepository.renamePackage(oldName, newName);
        done = true;
      }
      catch (ConstraintException cx) {
        Fx.error(getView(), MessageFormat.format(resources.getString("cannot override existing bundles in package {0}"), newName));
      }
    }
    else {
      Fx.error(getView(), MessageFormat.format(resources.getString("not a valid package name: {0}"), newName));
    }
    getStage().close();
  }

}
