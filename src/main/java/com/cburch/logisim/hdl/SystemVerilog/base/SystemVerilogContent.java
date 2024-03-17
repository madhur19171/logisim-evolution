/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.hdl.SystemVerilog.base;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.fpga.hdlgenerator.SystemVerilog;
import com.cburch.logisim.gui.generic.OptionPane;
import com.cburch.logisim.hdl.base.HdlContent;
import com.cburch.logisim.hdl.base.HdlModel;
import com.cburch.logisim.hdl.SystemVerilog.Strings;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.Softwares;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemVerilogContent extends HdlContent {

  public static class Generic extends SystemVerilogParser.GenericDescription {
    public Generic(SystemVerilogParser.GenericDescription g) {
      super(g.name, g.type, g.dval);
    }

    public Generic(Generic g) {
      super(g.name, g.type, g.dval);
    }
  }

  public static SystemVerilogContent create(String name, LogisimFile file) {
    final var content = new SystemVerilogContent(name, file);
    if (!content.setContent(TEMPLATE.replaceAll("%entityname%", name))) content.showErrors();
    return content;
  }

  public static SystemVerilogContent parse(String name, String systemVerilog, LogisimFile file) {
    final var content = new SystemVerilogContent(name, file);
    if (!content.setContent(systemVerilog)) content.showErrors();
    return content;
  }

  private static String loadTemplate() {
    final var input = SystemVerilogContent.class.getResourceAsStream(RESOURCE);
    final var in = new BufferedReader(new InputStreamReader(input));

    final var tmp = new StringBuilder();
    var line = "";
    try {
      while ((line = in.readLine()) != null) {
        tmp.append(line);
        tmp.append(System.getProperty("line.separator"));
      }
    } catch (IOException ex) {
      return "";
    } finally {
      try {
        if (input != null) input.close();
      } catch (IOException ex) {
        Logger.getLogger(SystemVerilogContent.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    return tmp.toString();
  }

  private static final String RESOURCE = "/resources/logisim/hdl/SystemVerilog_component.templ";

  private static final String TEMPLATE = loadTemplate();

  protected AttributeSet staticAttrs;
  protected StringBuilder content;
  protected boolean valid;
  protected final List<SystemVerilogParser.PortDescription> ports;
  protected Generic[] generics;
  protected List<Attribute<Integer>> genericAttrs;
  protected String name;
  protected AttributeOption appearance = StdAttr.APPEAR_EVOLUTION;
  protected String libraries;
  protected String architecture;
  private final LogisimFile logiFile;

  protected SystemVerilogContent(String name, LogisimFile file) {
    logiFile = file;
    this.name = name;
    ports = new ArrayList<>();
  }

  @Override
  public SystemVerilogContent clone() {
    try {
      SystemVerilogContent ret = (SystemVerilogContent) super.clone();
      ret.content = new StringBuilder(this.content);
      ret.valid = this.valid;
      return ret;
    } catch (CloneNotSupportedException ex) {
      return this;
    }
  }

  @Override
  public boolean compare(HdlModel model) {
    return compare(model.getContent());
  }

  @Override
  public boolean compare(String value) {
    return content
        .toString()
        .replaceAll("\\r\\n|\\r|\\n", " ")
        .equals(value.replaceAll("\\r\\n|\\r|\\n", " "));
  }

  @Override
  public boolean isValid() {
    return valid;
  }

  public String getArchitecture() {
    if (architecture == null) return "";

    return architecture;
  }

  @Override
  public String getContent() {
    return content.toString();
  }

  public Generic[] getGenerics() {
    if (generics == null) {
      return new Generic[0];
    }
    return generics;
  }

  public List<Attribute<Integer>> getGenericAttributes() {
    if (genericAttrs == null) {
      genericAttrs = new ArrayList<>();
      for (Generic g : getGenerics()) {
        genericAttrs.add(SystemVerilogModuleAttributes.forGeneric(g));
      }
    }
    return genericAttrs;
  }

  public String getLibraries() {
    if (libraries == null) return "";

    return libraries;
  }

  @Override
  public String getName() {
    if (name == null) return "";
    return name;
  }

  public AttributeOption getAppearance() {
    return appearance;
  }

  public void setAppearance(AttributeOption a) {
    appearance = a;
    fireAppearanceChanged();
  }

  public List<SystemVerilogParser.PortDescription> getPorts() {
    return ports;
  }

  public AttributeSet getStaticAttributes() {
    return staticAttrs;
  }

  public void aboutToSave() {
    fireAboutToSave();
  }

  static final String ENTITY_PATTERN = "(\\s*\\bentity\\s+)%entityname%(\\s+is)\\b";
  static final String ARCH_PATTERN = "(\\s*\\barchitecture\\s+\\w+\\s+of\\s+)%entityname%\\b";
  static final String END_PATTERN = "(\\s*\\bend\\s+)%entityname%(\\s*;)";

  /**
   * Check if a given label could be a valid SystemVerilog variable name
   *
   * @param label candidate SystemVerilog variable name
   * @return true if the label is NOT a valid name, false otherwise
   */
  public static boolean labelSystemVerilogInvalid(String label) {
    if (!label.matches("^[A-Za-z]\\w*") || label.endsWith("_") || label.matches(".*__.*"))
      return (true);
    return SystemVerilog.SystemVerilog_KEYWORDS.contains(label.toLowerCase());
  }

  public static boolean labelSystemVerilogInvalidNotify(String label, LogisimFile file) {
    String err = null;
    if (!label.matches("^[A-Za-z]\\w*") || label.endsWith("_") || label.matches(".*__.*")) {
      err = Strings.S.get("SystemVerilogInvalidNameError");
    } else if (SystemVerilog.SystemVerilog_KEYWORDS.contains(label.toLowerCase())) {
      err = Strings.S.get("SystemVerilogKeywordNameError");
    } else if (file != null && file.containsFactory(label)) {
      err = Strings.S.get("SystemVerilogDuplicateNameError");
    } else {
      return false;
    }
    OptionPane.showMessageDialog(
        null, label + ": " + err, Strings.S.get("validationParseError"), OptionPane.ERROR_MESSAGE);
    return true;
  }

  public boolean setName(String name) {
    if (name == null || labelSystemVerilogInvalidNotify(name, logiFile)) {
      return false;
    }
    final var entPat = ENTITY_PATTERN.replaceAll("%entityname%", this.name);
    final var archPat = ARCH_PATTERN.replaceAll("%entityname%", this.name);
    final var endPat = END_PATTERN.replaceAll("%entityname%", this.name);
    var str = content.toString();
    str = str.replaceAll("(?is)" + entPat, "$1" + name + "$2"); // entity NAME is
    str = str.replaceAll("(?is)" + archPat, "$1" + name); // architecture foo of NAME
    str = str.replaceAll("(?is)" + endPat, "$1" + name + "$2"); // end NAME ;
    return setContent(str);
  }

  private final StringBuilder errTitle = new StringBuilder();
  private final StringBuilder errMessage = new StringBuilder();
  private int errCode = 0;
  private Exception errException;

  @Override
  public void showErrors() {
    if (valid && errTitle.length() == 0 && errMessage.length() == 0) return;
    if (errException != null) errException.printStackTrace();
    if (errCode == Softwares.ERROR) {
      final var message = new JTextArea();
      message.setText(errMessage.toString());
      message.setEditable(false);
      message.setLineWrap(false);
      message.setMargin(new Insets(5, 5, 5, 5));

      final var sp = new JScrollPane(message);
      sp.setPreferredSize(new Dimension(700, 400));

      OptionPane.showOptionDialog(
          null,
          sp,
          errTitle.toString(),
          OptionPane.OK_OPTION,
          OptionPane.ERROR_MESSAGE,
          null,
          new String[] {Strings.S.get("validationErrorButton")},
          Strings.S.get("validationErrorButton"));
    } else if (errCode == Softwares.ABORT) {
      OptionPane.showMessageDialog(
          null, errMessage.toString(), errTitle.toString(), OptionPane.INFORMATION_MESSAGE);
    } else {
      OptionPane.showMessageDialog(
          null, errMessage.toString(), errTitle.toString(), OptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public boolean setContentNoValidation(String SystemVerilog) {
    if (valid && content.toString().equals(SystemVerilog)) return true;
    content = new StringBuilder(SystemVerilog);
    valid = false;

    return false;
  }

  @Override
  public boolean setContent(String SystemVerilog) {
    if (setContentNoValidation(SystemVerilog)) return true;

    try {
      errTitle.setLength(0);
      errMessage.setLength(0);
      errCode = Softwares.validateSystemVerilog(content.toString(), errTitle, errMessage);
      if (errCode != Softwares.SUCCESS) return false;

      final var parser = new SystemVerilogParser(content.toString());
      try {
        parser.parse();
      } catch (Exception ex) {
        var msg = ex.getMessage();
        if (msg == null || msg.length() == 0) msg = ex.toString();
        errTitle.append(Strings.S.get("validationParseError"));
        errMessage.append(msg);
        errException = ex;
        return false;
      }
      if (!parser.getName().equals(name)) {
        if (labelSystemVerilogInvalidNotify(parser.getName(), logiFile)) return false;
      } else {
        if (labelSystemVerilogInvalidNotify(parser.getName(), null)) return false;
      }

      valid = true;
      name = parser.getName();

      libraries = parser.getLibraries();
      architecture = parser.getArchitecture();

      ports.clear();
      ports.addAll(parser.getInputs());
      ports.addAll(parser.getOutputs());

      // If name and type is unchanged, keep old generic and attribute.
      final var oldGenerics = generics;
      final var oldAttrs = genericAttrs;

      generics = new Generic[parser.getGenerics().size()];
      genericAttrs = new ArrayList<>();
      var i = 0;
      for (final var g : parser.getGenerics()) {
        var found = false;
        if (oldGenerics != null) {
          for (var j = 0; j < oldGenerics.length; j++) {
            final var old = oldGenerics[j];
            if (old != null
                && old.getName().equals(g.getName())
                && old.getType().equals(g.getType())) {
              generics[i] = old;
              oldGenerics[j] = null;
              genericAttrs.add(oldAttrs.get(j));
              found = true;
              break;
            }
          }
        }
        if (!found) {
          generics[i] = new Generic(g);
          genericAttrs.add(SystemVerilogModuleAttributes.forGeneric(generics[i]));
        }
        i++;
      }

      staticAttrs = SystemVerilogModuleAttributes.createBaseAttrs(this);

      valid = true;
      return true;
    } finally {
      fireContentSet();
    }
  }
}
