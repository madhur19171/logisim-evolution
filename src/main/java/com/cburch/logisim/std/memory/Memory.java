/*
 * This file is part of logisim-evolution.
 *
 * Logisim-evolution is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Logisim-evolution is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with logisim-evolution. If not, see <http://www.gnu.org/licenses/>.
 *
 * Original code by Carl Burch (http://www.cburch.com), 2011.
 * Subsequent modifications by:
 *   + College of the Holy Cross
 *     http://www.holycross.edu
 *   + Haute École Spécialisée Bernoise/Berner Fachhochschule
 *     http://www.bfh.ch
 *   + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *     http://hepia.hesge.ch/
 *   + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *     http://www.heig-vd.ch/
 */

package com.cburch.logisim.std.memory;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import java.util.List;

public class Memory extends Library {

  /**
   * Unique identifier of the library, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   *
   * Identifier value must MUST be unique string among all libraries.
   */
  public static final String _ID = "Memory";

  protected static final int DELAY = 5;

  private static final FactoryDescription[] DESCRIPTIONS = {
    new FactoryDescription(
        "D Flip-Flop", S.getter("dFlipFlopComponent"), "dFlipFlop.gif", DFlipFlop.class),
    new FactoryDescription(
        "T Flip-Flop", S.getter("tFlipFlopComponent"), "tFlipFlop.gif", TFlipFlop.class),
    new FactoryDescription(
        "J-K Flip-Flop", S.getter("jkFlipFlopComponent"), "jkFlipFlop.gif", JKFlipFlop.class),
    new FactoryDescription(
        "S-R Flip-Flop", S.getter("srFlipFlopComponent"), "srFlipFlop.gif", SRFlipFlop.class),
    new FactoryDescription("Register", S.getter("registerComponent"), "register.gif", Register.class),
    new FactoryDescription("Counter", S.getter("counterComponent"), "counter.gif", Counter.class),
    new FactoryDescription(
        "Shift Register", S.getter("shiftRegisterComponent"), "shiftreg.gif", ShiftRegister.class),
    new FactoryDescription("Random", S.getter("randomComponent"), "random.gif", Random.class),
    new FactoryDescription("RAM", S.getter("ramComponent"), "ram.gif", Ram.class),
    new FactoryDescription("ROM", S.getter("romComponent"), "rom.gif", Rom.class),
  };

  private List<Tool> tools = null;

  @Override
  public String getDisplayName() {
    return S.get("memoryLibrary");
  }

  @Override
  public List<Tool> getTools() {
    if (tools == null) {
      tools = FactoryDescription.getTools(Memory.class, DESCRIPTIONS);
    }
    return tools;
  }
}
