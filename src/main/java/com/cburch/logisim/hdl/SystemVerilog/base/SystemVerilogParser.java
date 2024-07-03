/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.hdl.SystemVerilog.base;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.instance.Port;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.cburch.logisim.std.Strings.S;

public class SystemVerilogParser {
  public static class IllegalSystemVerilogContentException extends Exception {

    private static final long serialVersionUID = 1L;

    public IllegalSystemVerilogContentException() {
      super();
    }

    public IllegalSystemVerilogContentException(String message) {
      super(message);
    }

    public IllegalSystemVerilogContentException(String message, Throwable cause) {
      super(message, cause);
    }

    public IllegalSystemVerilogContentException(Throwable cause) {
      super(cause);
    }
  }

  public static class PortDescription {

    private final String name;
    private final String type;
    private final BitWidth width;

    public PortDescription(String name, String type, int width) {
      this.name = name;
      this.type = type;
      this.width = BitWidth.create(width);
    }

    public String getName() {
      return this.name;
    }

    public String getType() {
      return this.type;
    }

    public String getSystemVerilogType() {
      return switch (type) {
        case Port.INPUT -> "input";
        case Port.OUTPUT -> "output";
        case Port.INOUT -> "inout";
        default -> throw new IllegalArgumentException("Not recognized port type: " + type);
      };
    }

    public BitWidth getWidth() {
      return this.width;
    }
  }

  public static class ParameterDescription {

    protected final String name;
    protected final int value;

    public ParameterDescription(String name, int value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return this.name;
    }

    public int getValue() {
      return this.value;
    }
  }

  private static Pattern regex(String pattern) {
    pattern = pattern.trim();
    pattern = "^ " + pattern;
    pattern = pattern.replaceAll(" {2}", "\\\\s+"); // Two spaces = required whitespace
    pattern = pattern.replaceAll(" ", "\\\\s*"); // One space = optional whitespace
    return Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  }

  private static final Pattern MODULE = Pattern.compile("module\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern END_MODULE = Pattern.compile("endmodule", Pattern.CASE_INSENSITIVE);
  private static final Pattern PORT = Pattern.compile("(input|output|inout)\\s+(wire|reg)?\\s*\\[?(\\d*:?\\d*)?\\]?\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern PORTS = Pattern.compile("\\(", Pattern.CASE_INSENSITIVE);
  private static final Pattern OPENLIST = Pattern.compile("\\(", Pattern.CASE_INSENSITIVE);
  private static final Pattern SEMICOLON = Pattern.compile(";", Pattern.CASE_INSENSITIVE);
  private static final Pattern DONELIST = Pattern.compile("\\)", Pattern.CASE_INSENSITIVE);
  private static final Pattern PARAMETERS = Pattern.compile("#\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
  private static final Pattern PARAMETER = Pattern.compile("parameter\\s+(\\w+)\\s*=\\s*([^,\\)]+)", Pattern.CASE_INSENSITIVE);

  private static final Pattern DVALUE = regex(":= (\\w+)");

  private final List<PortDescription> inputs;
  private final List<PortDescription> outputs;
  private final List<ParameterDescription> parameters;
  private final String source;
  private String name;
  private String packages;

  public SystemVerilogParser(String source) {
    this.source = source;
    this.inputs = new ArrayList<>();
    this.outputs = new ArrayList<>();
    this.parameters = new ArrayList<>();
  }

  private int getEOLIndex(String input, int from) {
    int index;

    index = input.indexOf("\n", from);
    if (index != -1) return index;

    index = input.indexOf("\r\n", from);
    if (index != -1) return index;

    index = input.indexOf("\r", from);
    if (index != -1) return index;

    return input.length();
  }

  public List<PortDescription> getInputs() {
    return inputs;
  }

  public List<PortDescription> getOutputs() {
    return outputs;
  }

  public List<ParameterDescription> getParameters() {
    return parameters;
  }

  public String getPackages() {
    return packages;
  }

  public String getName() {
    return name;
  }

  private String getPortType(String type) throws IllegalSystemVerilogContentException {
    if ("input".equalsIgnoreCase(type)) return Port.INPUT;
    if ("output".equalsIgnoreCase(type)) return Port.OUTPUT;
    if ("inout".equalsIgnoreCase(type)) return Port.INOUT;

    throw new IllegalSystemVerilogContentException(S.get("invalidTypeException") + ": " + type);
  }

  public void parse() throws IllegalSystemVerilogContentException {
    final var input = new Scanner(removeComments());
    // TODO: Parse packages
    if (input.findWithinHorizon(MODULE, 0) == null) {
      throw new IllegalSystemVerilogContentException("Cannot find module");
    }
    name = input.match().group(1);

    parseParameters(input);

    parsePorts(input);

    // Ensure the module is properly closed with 'endmodule'
    if (input.findWithinHorizon(END_MODULE, 0) == null) {
      throw new IllegalSystemVerilogContentException(S.get("infiniteModuleException"));
    }
  }

  private void parsePort(Scanner input) throws IllegalSystemVerilogContentException {
    // Example: "input wire [7:0] data_in"
    // Example: "output reg data_out"
    // Example: "inout wire signal"

    if (input.findWithinHorizon(PORT, 0) == null) throw new IllegalSystemVerilogContentException(S.get("portDeclarationException"));

    String direction = input.match().group(1).trim();
    String type = input.match().group(2) != null ? input.match().group(2).trim() : "wire";
    String range = input.match().group(3) != null ? input.match().group(3).trim() : null;
    String names = input.match().group(4).trim();

    int width = 1;
    if (range != null) {
      // Example range: "[7:0]"
      String[] bounds = range.split(":");
      int upper = Integer.parseInt(bounds[0].replaceAll("[\\[\\]]", ""));
      int lower = Integer.parseInt(bounds[1].replaceAll("[\\[\\]]", ""));
      width = upper - lower + 1;
    }

    for (String name : names.split("\\s*,\\s*")) {
      if (direction.equalsIgnoreCase("input"))
        inputs.add(new PortDescription(name, Port.INPUT, width));
      else if (direction.equalsIgnoreCase("output"))
        outputs.add(new PortDescription(name, Port.OUTPUT, width));
    }
  }

  private void parsePorts(Scanner input) throws IllegalSystemVerilogContentException {
    // Example: "module (...);"
    // Example: "module (...; ...; ...);"

    //TODO: Not the best way to parse ports.
    //  () can mean anything.
    //  (parameter *****) should be matched instead

    if (input.findWithinHorizon(OPENLIST, 0) == null)
      throw new IllegalSystemVerilogContentException(S.get("portDeclarationException"));
    parsePort(input);
    while (input.findWithinHorizon(SEMICOLON, 0) != null) parsePort(input);
    if (input.findWithinHorizon(DONELIST, 0) == null)
      throw new IllegalSystemVerilogContentException(S.get("portDeclarationException"));
  }

  // TODO: Very simple Parameter support. Does not support parameter type of bit width yet.
  //       Only Integers supported in parameter
  private boolean parseParameters(Scanner input) throws IllegalSystemVerilogContentException {
    // Example: #(parameter PARAM1 = VALUE1, parameter PARAM2 = VALUE2)

    if (input.findWithinHorizon(PARAMETERS, 0) == null) return false;

    String param = input.match().group(1);
    Scanner parameterScanner = new Scanner(param);

    while (parameterScanner.findWithinHorizon(PARAMETER, 0) != null) {
      String paramName = parameterScanner.match().group(1);
      String paramValue = parameterScanner.match().group(2);

      parameters.add(new ParameterDescription(paramName, Integer.parseInt(paramValue)));
    }

    return true;
  }

  private String removeComments() throws IllegalSystemVerilogContentException {
    StringBuilder input;
    try {
      input = new StringBuilder(source);
    } catch (NullPointerException ex) {
      throw new IllegalSystemVerilogContentException(S.get("emptySourceException"));
    }

    int from;
    // TODO: Support for /**/ type comments
    while ((from = input.indexOf("//")) != -1) {
      int to = getEOLIndex(input.toString(), from);
      input.delete(from, to);
    }

    return input.toString().trim();
  }
}
