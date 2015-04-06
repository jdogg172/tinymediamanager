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
package org.tinymediamanager.ui.plaf.light;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseBorders;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.luna.LunaBorders.RolloverToolButtonBorder;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author Manuel Laggner
 */
public class TmmLightBorders extends BaseBorders {
  protected static Border titledBorder = null;

  // ------------------------------------------------------------------------------------
  // Lazy access methods
  // ------------------------------------------------------------------------------------
  public static Border getTextBorder() {
    if (textFieldBorder == null) {
      textFieldBorder = new TextFieldBorder();
    }
    return textFieldBorder;
  }

  public static Border getTextFieldBorder() {
    return getTextBorder();
  }

  public static Border getComboBoxBorder() {
    if (comboBoxBorder == null) {
      comboBoxBorder = new TextFieldBorder();
    }
    return comboBoxBorder;
  }

  public static Border getScrollPaneBorder() {
    if (scrollPaneBorder == null) {
      scrollPaneBorder = new ScrollPaneBorder(false);
    }
    return scrollPaneBorder;
  }

  public static Border getTableScrollPaneBorder() {
    if (tableScrollPaneBorder == null) {
      tableScrollPaneBorder = new ScrollPaneBorder(true);
    }
    return tableScrollPaneBorder;
  }

  public static Border getButtonBorder() {
    if (buttonBorder == null) {
      buttonBorder = new EmptyBorder(4, 15, 4, 15);
    }
    return buttonBorder;
  }

  public static Border getToggleButtonBorder() {
    return getButtonBorder();
  }

  public static Border getRolloverToolButtonBorder() {
    if (rolloverToolButtonBorder == null) {
      rolloverToolButtonBorder = new RolloverToolButtonBorder();
    }
    return rolloverToolButtonBorder;
  }

  public static Border getInternalFrameBorder() {
    if (internalFrameBorder == null) {
      internalFrameBorder = new InternalFrameBorder();
    }
    return internalFrameBorder;
  }

  public static Border getTableHeaderBorder() {
    if (tableHeaderBorder == null) {
      tableHeaderBorder = new TableHeaderBorder();
    }
    return tableHeaderBorder;
  }

  public static Border getPopupMenuBorder() {
    if (popupMenuBorder == null) {
      popupMenuBorder = new PopupMenuBorder();
    }
    return popupMenuBorder;
  }

  public static Border getSpinnerBorder() {
    if (spinnerBorder == null) {
      spinnerBorder = new TextFieldBorder();
    }
    return spinnerBorder;
  }

  public static Border getTitledBorder() {
    if (titledBorder == null) {
      titledBorder = new RoundLineBorder(ColorHelper.brighter(AbstractLookAndFeel.getForegroundColor(), 30), 1, 16);
    }
    return titledBorder;
  }

  // ------------------------------------------------------------------------------------
  // Implementation of border classes
  // ------------------------------------------------------------------------------------
  public static class TextFieldBorder extends AbstractBorder implements UIResource {
    private static final long   serialVersionUID = -1476629322366320255L;
    private static final Insets insets           = new Insets(4, 6, 5, 7);
    private static final Color  SHADOW_COLOR     = new Color(208, 208, 208);

    private static int          focusWidth       = 2;

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      int r = 10;
      Container parent = c.getParent();
      if (parent != null) {
        RoundRectangle2D round = new RoundRectangle2D.Float(x + focusWidth, y + focusWidth, width - 2 * focusWidth, height - 2 * focusWidth, r, r);
        RoundRectangle2D shadow = new RoundRectangle2D.Float(x + focusWidth + 1, y + focusWidth + 1, width - 2 * focusWidth, height - 2 * focusWidth,
            r, r);
        GraphicsConfiguration gc = ((Graphics2D) g).getDeviceConfiguration();
        BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, width, height);

        Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
        g2.setComposite(AlphaComposite.Src);
        g2.setColor(parent.getBackground());
        corner.subtract(new Area(round));
        g2.fill(corner);

        g2.setColor(SHADOW_COLOR);
        corner.intersect(new Area(shadow));

        // drop shadow only when the component is opaque
        if (c.isOpaque()) {
          g2.fill(corner);
        }

        boolean focus = c.hasFocus();
        if (c instanceof JSpinner) {
          Component[] comps = ((JSpinner) c).getEditor().getComponents();
          for (Component component : comps) {
            focus |= component.hasFocus();
          }
        }
        if (focus) {
          x = focusWidth;
          y = focusWidth;
          int w = width - 2 * focusWidth;
          int h = height - 2 * focusWidth;
          g2.setColor(AbstractLookAndFeel.getFocusColor());
          for (int i = focusWidth; i > 0; i -= 1) {
            final float opacity = (float) (1 - (2.f * i * i / 10));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
            g2.fillRoundRect(x - i, y - i, w + 2 * i, h + 2 * i, r, r);
          }
        }
        g2.dispose();
        g.drawImage(img, 0, 0, null);
      }
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
      return TextFieldBorder.insets;
    }
  } // class TextFieldBorder

  public static class ScrollPaneBorder extends AbstractBorder implements UIResource {
    private static final long   serialVersionUID = -7118022577788519656L;
    private static final Color  fieldBorderColor = new Color(127, 157, 185);
    private static final Insets insets           = new Insets(2, 2, 2, 2);
    private static final Insets tableInsets      = new Insets(1, 1, 1, 1);
    private boolean             tableBorder      = false;

    public ScrollPaneBorder(boolean tableBorder) {
      this.tableBorder = tableBorder;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      if (tableBorder) {
        g.setColor(fieldBorderColor);
        g.drawRect(x, y, w - 1, h - 1);
        g.setColor(ColorHelper.brighter(AbstractLookAndFeel.getTheme().getBackgroundColor(), 50));
        g.drawRect(x + 1, y + 1, w - 3, h - 3);
      }
      else {
        Container parent = c.getParent();
        if (parent != null) {
          int r = 16;
          RoundRectangle2D round = new RoundRectangle2D.Float(x, y, w, h, r, r);
          GraphicsConfiguration gc = ((Graphics2D) g).getDeviceConfiguration();
          BufferedImage img = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
          Graphics2D g2 = img.createGraphics();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setComposite(AlphaComposite.Clear);
          g2.fillRect(0, 0, w, h);

          Area corner = new Area(new Rectangle2D.Float(x, y, w, h));
          g2.setComposite(AlphaComposite.Src);
          g2.setColor(parent.getBackground());
          corner.subtract(new Area(round));
          g2.fill(corner);
          g2.dispose();
          g.drawImage(img, 0, 0, null);
        }
      }
    }

    @Override
    public Insets getBorderInsets(Component c) {
      if (tableBorder) {
        return new Insets(tableInsets.top, tableInsets.left, tableInsets.bottom, tableInsets.right);
      }
      else {
        return new Insets(insets.top, insets.left, insets.bottom, insets.right);
      }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      Insets ins = getBorderInsets(c);
      borderInsets.left = ins.left;
      borderInsets.top = ins.top;
      borderInsets.right = ins.right;
      borderInsets.bottom = ins.bottom;
      return borderInsets;
    }
  } // class ScrollPaneBorder

  public static class InternalFrameBorder extends BaseInternalFrameBorder {
    private static final long serialVersionUID = 1227394113801329301L;

    public InternalFrameBorder() {
      insets.top = 3;
      insets.left = 2;
      insets.right = 2;
      insets.bottom = 2;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      g.setColor(Color.BLACK);
      g.fillRect(x, y, w, h);
      g.setColor(AbstractLookAndFeel.getWindowBorderColor());
      g.fillRect(x + 1, y + 1, w - 2, h - 2);
    }
  } // class InternalFrameBorder

  public static class TableHeaderBorder extends AbstractBorder implements UIResource {
    private static final long   serialVersionUID = -2182436739429673033L;
    private static final Insets insets           = new Insets(0, 1, 1, 1);

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      g.setColor(ColorHelper.brighter(AbstractLookAndFeel.getControlBackgroundColor(), 40));
      g.drawLine(0, 0, 0, h - 1);
      g.setColor(ColorHelper.darker(AbstractLookAndFeel.getControlBackgroundColor(), 20));
      g.drawLine(w - 1, 0, w - 1, h - 1);
      g.setColor(ColorHelper.darker(AbstractLookAndFeel.getControlBackgroundColor(), 10));
      g.drawLine(0, h - 1, w - 1, h - 1);
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      borderInsets.left = insets.left;
      borderInsets.top = insets.top;
      borderInsets.right = insets.right;
      borderInsets.bottom = insets.bottom;
      return borderInsets;
    }
  } // class TableHeaderBorder

  public static class PopupMenuBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID  = -2851747427345778378L;
    protected static Insets   insets;

    protected static int      TOP_BOTTOM_INSETS = 10;

    public PopupMenuBorder() {
      insets = new Insets(TOP_BOTTOM_INSETS, 1, 2 * TOP_BOTTOM_INSETS, 1);

    }

    public boolean isMenuBarPopup(Component c) {
      boolean menuBarPopup = false;
      if (c instanceof JPopupMenu) {
        JPopupMenu pm = (JPopupMenu) c;
        if (pm.getInvoker() != null) {
          menuBarPopup = (pm.getInvoker().getParent() instanceof JMenuBar);
        }
      }
      return menuBarPopup;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      Color borderColorLo = AbstractLookAndFeel.getGridColor();// getFrameColor();

      Graphics2D g2D = (Graphics2D) g;
      Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // - outer frame
      g.setColor(borderColorLo);
      if (isMenuBarPopup(c)) {
        // top
        g.drawLine(x - 1, y, x + w, y);
        // left
        g.drawLine(x, y, x, y + h - 1);
        // bottom
        g.drawLine(x, y + h - 1, x + w, y + h - 1);
        // right
        g.drawLine(x + w - 1, y + 1, x + w - 1, y + h - 1);
      }
      else {
        g.drawRect(x, y, w - 1, h - 1);
      }

      // paint the bottom border in the default panel color
      g.setColor(AbstractLookAndFeel.getBackgroundColor());
      g.fillRect(x + 1, y + h - insets.bottom, w - 2, insets.bottom - 1);

      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      Insets ins = getBorderInsets(c);
      borderInsets.left = ins.left;
      borderInsets.top = ins.top;
      borderInsets.right = ins.right;
      borderInsets.bottom = ins.bottom;
      return borderInsets;
    }

  } // class PopupMenuBorder

  public static class RoundLineBorder extends LineBorder {
    protected int radius;

    public RoundLineBorder(Color color, int thickness, int radius) {
      super(color, thickness, true);
      this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      if ((this.thickness > 0) && (g instanceof Graphics2D)) {
        Graphics2D g2d = (Graphics2D) g;

        Object savedRederingHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color oldColor = g2d.getColor();
        Stroke oldKeyStroke = g2d.getStroke();
        g2d.setColor(this.lineColor);

        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawRoundRect(x, y, width - thickness, height - thickness, radius, radius);

        g2d.setColor(oldColor);
        g2d.setStroke(oldKeyStroke);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
      }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      Insets ins = getBorderInsets(c);
      borderInsets.left = ins.left;
      borderInsets.top = ins.top;
      borderInsets.right = ins.right;
      borderInsets.bottom = ins.bottom;
      return borderInsets;
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(0, radius / 2, radius / 2, radius / 2);
    }
  } // class RoundLineBorder

} // class TmmLightBorders