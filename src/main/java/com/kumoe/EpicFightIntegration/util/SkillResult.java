package com.kumoe.EpicFightIntegration.util;

import java.util.Map;

public class SkillResult {
    Map<String, Map<String, Integer>> noMeetReqs;
    boolean result;

    public SkillResult(Map<String, Map<String, Integer>> noMeetReqs, boolean result){
        this.noMeetReqs = noMeetReqs;
        this.result = result;
    }

    public Map<String, Map<String, Integer>> getNoMeetReqs() {
        return noMeetReqs;
    }

    public boolean getResult() {
        return result;
    }
}
