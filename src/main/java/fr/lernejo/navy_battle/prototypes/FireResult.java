package fr.lernejo.navy_battle.prototypes;

public enum FireResult {
    MISS("miss"), HIT("hit"), SUNK("sunk");

    private final String apiString;

    FireResult(String res) {
        this.apiString = res;
    }

    public String toAPI() {
        return apiString;
    }
}
