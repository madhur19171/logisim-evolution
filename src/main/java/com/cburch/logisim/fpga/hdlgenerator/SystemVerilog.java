package com.cburch.logisim.fpga.hdlgenerator;

import com.cburch.logisim.prefs.AppPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SystemVerilog {

  // TODO: Change all Reserved Words
  private static final String[] RESERVED_SystemVerilog_WORDS = {
    "abs",
    "all",
    "access",
    "after",
    "alias",
    "all",
    "and",
    "architecture",
    "array",
    "assert",
    "attribute",
    "begin",
    "block",
    "body",
    "buffer",
    "bus",
    "case",
    "component",
    "configuration",
    "constant",
    "disconnect",
    "downto",
    "else",
    "elsif",
    "end",
    "entity",
    "exit",
    "file",
    "for",
    "function",
    "generate",
    "generic",
    "group",
    "guarded",
    "if",
    "integer",
    "impure",
    "in",
    "inertial",
    "inout",
    "is",
    "label",
    "library",
    "linkage",
    "literal",
    "loop",
    "map",
    "mod",
    "nand",
    "new",
    "next",
    "nor",
    "not",
    "null",
    "of",
    "on",
    "open",
    "or",
    "others",
    "out",
    "package",
    "port",
    "postponed",
    "procedure",
    "process",
    "pure",
    "range",
    "record",
    "register",
    "reject",
    "rem",
    "report",
    "return",
    "rol",
    "ror",
    "select",
    "severity",
    "signal",
    "shared",
    "sla",
    "sll",
    "sra",
    "srl",
    "subtype",
    "then",
    "to",
    "transport",
    "type",
    "unaffected",
    "units",
    "until",
    "use",
    "variable",
    "wait",
    "when",
    "while",
    "with",
    "xnor",
    "xor"
  };

  public static final List<String> SystemVerilog_KEYWORDS = Arrays.asList(RESERVED_SystemVerilog_WORDS);

  public static Set<String> getSystemVerilogKeywords() {
    final var keywords = new TreeSet<String>();
    for (final var keyword : SystemVerilog_KEYWORDS)
      keywords.add(AppPreferences.SystemVerilogKeywordsUpperCase.get() ? keyword.toUpperCase() : keyword);
    return keywords;
  }

  public static String getVhdlKeyword(String keyword) {
    final var spaceStrippedKeyword = keyword.replace(" ", "").toLowerCase();
    if (SystemVerilog_KEYWORDS.contains(spaceStrippedKeyword))
      return AppPreferences.SystemVerilogKeywordsUpperCase.get()
          ? keyword.toUpperCase()
          : keyword.toLowerCase();
    throw new IllegalArgumentException("An unknown VHDL keyword was passed!");
  }
}
