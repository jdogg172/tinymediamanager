/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui.plaf.light;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseTabbedPaneUI;
import com.jtattoo.plaf.luna.LunaTabbedPaneUI;

/**
 * The Class TmmLightTabbedPaneUI.
 * 
 * @author Manuel Laggner
 */
public class TmmLightTabbedPaneUI extends BaseTabbedPaneUI {

  protected static int BORDER_RADIUS = 15;
  protected static int TAB_GAP       = 2;

  public static ComponentUI createUI(JComponent c) {
    Object prop = c.getClientProperty("class");
    if (prop != null && prop instanceof String && "big".equals(prop.toString())) {
      return new TmmLightTabbedPaneUI();
    }
    return new LunaTabbedPaneUI();
  }

  @Override
  public void installDefaults() {
    super.installDefaults();
    tabInsets = new Insets(0, 20, 0, 20);
  }

  @Override
  protected Font getTabFont(boolean isSelected) {
    return super.getTabFont(isSelected).deriveFont(16f);
  }

  @Override
  protected FontMetrics getFontMetrics() {
    Font font = getTabFont(false);
    return Toolkit.getDefaultToolkit().getFontMetrics(font);
  }

  @Override
  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    Graphics2D g2D = (Graphics2D) g;
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (isSelected) {
      g.setColor(AbstractLookAndFeel.getBackgroundColor());
    }
    else {
      g.setColor(new Color(163, 163, 163));
    }

    if (tabPlacement == TOP) {
      g.fillRoundRect(x + TAB_GAP, y, w - 2 * TAB_GAP, h, BORDER_RADIUS, BORDER_RADIUS);
      g.fillRect(x + TAB_GAP, y + BORDER_RADIUS, w - 2 * TAB_GAP, h);
      // g.fillRect(x + 1, y + 1, w - 1, h + 2);
    }
    else if (tabPlacement == LEFT) {
      g.fillRect(x + 1, y + 1, w + 2, h - 1);
    }
    else if (tabPlacement == BOTTOM) {
      g.fillRect(x + 1, y - 2, w - 1, h + 2);
    }
    else {
      g.fillRect(x - 2, y + 1, w + 2, h - 1);
    }

    g2D.setRenderingHints(savedRenderingHints);
  }

  @Override
  protected void paintRoundedTopTabBorder(int tabIndex, Graphics g, int x1, int y1, int x2, int y2, boolean isSelected) {
  }

  @Override
  protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
  }

  @Override
  protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect,
      boolean isSelected) {
  }

  @Override
  protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect,
      Rectangle textRect, boolean isSelected) {
    super.layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);

    textRect.y += (metrics.getDescent() / 2);
  }

}
