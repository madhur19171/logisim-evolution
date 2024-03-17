/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.hdl.SystemVerilog.base;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.hdl.SystemVerilog.Strings;
import com.cburch.logisim.std.hdl.SystemVerilog.SystemVerilogEntityComponent;
import com.cburch.logisim.util.StringGetter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class SystemVerilogConstants {

  public static class SystemVerilogSimNameAttribute extends Attribute<String> {

    private SystemVerilogSimNameAttribute(String name, StringGetter disp) {
      super(name, disp);
    }

    @Override
    public String parse(String value) {
      return value;
    }

    @Override
    public boolean isHidden() {
      return true;
    }
  }

  public static List<Component> getSystemVerilogComponents(CircuitState s, boolean newStyle) {

    LinkedList<Component> SystemVerilogComp = new LinkedList<>();

    /* Add current circuits comp */
    for (Component comp : s.getCircuit().getNonWires()) {
      if (comp.getFactory().getClass().equals(SystemVerilogEntityComponent.class)) {
        SystemVerilogComp.add(comp);
      }
      if (comp.getFactory().getClass().equals(SystemVerilogModule.class) && newStyle) {
        SystemVerilogComp.add(comp);
      }
    }

    /* Add subcircuits comp */
    for (CircuitState sub : s.getSubStates()) {
      SystemVerilogComp.addAll(getSystemVerilogComponents(sub, newStyle));
    }

    return SystemVerilogComp;
  }

  public enum State {
    DISABLED,
    ENABLED,
    STARTING,
    RUNNING
  }

  public static final Charset ENCODING = StandardCharsets.UTF_8;
  public static final String SystemVerilog_TEMPLATES_PATH = "/resources/logisim/hdl/";
  public static final String SIM_RESOURCES_PATH = "/resources/logisim/sim/";
  public static final String SIM_PATH = System.getProperty("java.io.tmpdir") + "/logisim/sim/";
  public static final String SIM_SRC_PATH = SIM_PATH + "src/";
  public static final String SIM_COMP_PATH = SIM_PATH + "comp/";
  public static final String SIM_TOP_FILENAME = "top_sim.sv";
  public static final String SystemVerilog_COMPONENT_SIM_NAME = "LogisimSystemVerilogSimComp_";
  // FIXME: hardcoded path. The "../src/" asks for troubles!
  public static final String SystemVerilog_COMPILE_COMMAND = "vcom -reportprogress 300 -work work ../src/"; // TODO: Change
  public static final SystemVerilogSimNameAttribute SIM_NAME_ATTR =
      new SystemVerilogSimNameAttribute("SystemVerilogSimName", Strings.S.getter("SystemVerilogSimName"));
}
