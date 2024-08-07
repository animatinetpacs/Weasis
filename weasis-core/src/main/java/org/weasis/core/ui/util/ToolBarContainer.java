/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.core.ui.util;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.api.util.ResourceUtil;

public class ToolBarContainer extends JPanel {
  public static final Toolbar EMPTY = buildEmptyToolBar("empty");
  private final List<Toolbar> bars = new ArrayList<>();

  public ToolBarContainer() {
    setLayout(new WrapLayout(FlowLayout.LEADING, 0, 0));
    addMouseListener(new PopClickListener());
  }

  /** Registers a new ToolBar. */
  public void registerToolBar(List<Toolbar> toolBars) {
    unregisterAll();

    if (toolBars == null || toolBars.isEmpty()) {
      add(ToolBarContainer.EMPTY.getComponent());
      bars.add(ToolBarContainer.EMPTY);
    } else {
      // Sort toolbars according the position
      InsertableUtil.sortInsertable(toolBars);

      synchronized (toolBars) { // NOSONAR lock object is the list for iterating its elements safely
        for (Toolbar b : toolBars) {
          WtoolBar bar = b.getComponent();
          if (bar instanceof DynamicToolbar dyn) {
            dyn.updateToolbar();
          }
          if (bar.isComponentEnabled()) {
            add(bar);
          }
          bars.add(b);
        }
      }
    }

    revalidate();
    repaint();
  }

  public void displayToolbar(WtoolBar bar, boolean show) {
    if (show != bar.isComponentEnabled()) {
      if (show) {
        int barIndex = bar.getComponentPosition();
        int insert = 0;
        for (Insertable b : bars) {
          if (b.isComponentEnabled() && b.getComponentPosition() < barIndex) {
            insert++;
          }
        }
        if (insert >= getComponentCount()) {
          // -1 => inserting after the last component
          insert = -1;
        }
        add(bar, insert);
      } else {
        super.remove(bar);
      }
      bar.setComponentEnabled(show);
      revalidate();
      repaint();
    }
  }

  private void unregisterAll() {
    bars.clear();
    removeAll();
  }

  /**
   * Returns the list of currently registered toolbars.
   *
   * <p>returns a new list at each invocation.
   */
  public List<Toolbar> getRegisteredToolBars() {
    return new ArrayList<>(bars);
  }

  class PopClickListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) doPop(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) doPop(e);
    }

    private void doPop(MouseEvent e) {
      PopUpToolbars menu = new PopUpToolbars();
      menu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  class PopUpToolbars extends JPopupMenu {
    public PopUpToolbars() {
      for (final Toolbar bar : getRegisteredToolBars()) {
        if (!Insertable.Type.EMPTY.equals(bar.getType())) {
          JCheckBoxMenuItem item =
              new JCheckBoxMenuItem(bar.getComponentName(), bar.isComponentEnabled());
          item.addActionListener(
              e -> {
                if (e.getSource() instanceof JCheckBoxMenuItem menuItem) {
                  displayToolbar(bar.getComponent(), menuItem.isSelected());
                }
              });
          add(item);
        }
      }
    }
  }

  public static WtoolBar buildEmptyToolBar(String name) {
    WtoolBar toolBar =
        new WtoolBar(name, 0) {
          @Override
          public Type getType() {
            return Type.EMPTY;
          }
        };
    toolBar.add(buildToolBarSizerComponent());
    return toolBar;
  }

  private static JComponent buildToolBarSizerComponent() {
    return new JButton(
        new Icon() {

          @Override
          public void paintIcon(Component c, Graphics g, int x, int y) {
            // Do noting
          }

          @Override
          public int getIconWidth() {
            return 2;
          }

          @Override
          public int getIconHeight() {
            return ResourceUtil.TOOLBAR_ICON_SIZE;
          }
        });
  }
}
