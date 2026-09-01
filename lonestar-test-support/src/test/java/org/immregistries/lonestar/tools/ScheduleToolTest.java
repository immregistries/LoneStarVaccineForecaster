package org.immregistries.lonestar.tools;

import org.junit.jupiter.api.Test;

class ScheduleToolTest {

  @Test
  void validatesAllCheckedInScheduleSources() throws Exception {
    ScheduleTool.main(new String[] {"validate"});
  }
}