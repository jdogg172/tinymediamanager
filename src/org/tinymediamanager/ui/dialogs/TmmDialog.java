/*
 * Copyright 2012 - 2014 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.dialogs;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.jdesktop.beansbinding.Binding;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;

/**
 * The class TmmDialog. The abstract super class to handle all dialogs in tMM
 * 
 * @author Manuel Laggner
 */
public abstract class TmmDialog extends JDialog {
  private static final long           serialVersionUID = 1L;

  protected List<Binding<?, ?, ?, ?>> bindings;

  public TmmDialog(String title, String id) {
    super(MainWindow.getActiveInstance());
    setTitle(title);
    setName(id);
    setIconImage(MainWindow.LOGO);
    setModal(true);
    setModalityType(ModalityType.APPLICATION_MODAL);
    bindings = new ArrayList<>();
  }

  @Override
  protected JRootPane createRootPane() {
    JRootPane rootPane = super.createRootPane();
    KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
    Action actionListener = new AbstractAction() {
      private static final long serialVersionUID = 3943345336176709047L;

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        setVisible(false);
      }
    };

    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(stroke, "ESCAPE");
    rootPane.getActionMap().put("ESCAPE", actionListener);

    return rootPane;
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      TmmWindowSaver.getInstance().loadSettings(this);
      pack();
      setLocationRelativeTo(MainWindow.getActiveInstance());
      super.setVisible(true);
    }
    else {
      for (Binding<?, ?, ?, ?> binding : bindings) {
        if (binding.isBound()) {
          binding.unbind();
        }
      }

      bindings.clear();

      super.setVisible(false);
      dispose();
    }
  }

  /**
   * safely unbind bindings
   * 
   * @param binding
   *          to be unbound
   */
  protected void safeUnbind(Binding<?, ?, ?, ?> binding) {
    if (binding != null && binding.isBound()) {
      binding.unbind();
    }
  }
}
