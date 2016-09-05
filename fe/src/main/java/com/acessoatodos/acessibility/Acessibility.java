package com.acessoatodos.acessibility;

/**
 * API response holding accessibility information.
 */
public enum Acessibility {

    ACCESS_RAMP(100),
    ADAPTED_WC(101),
    ELEVATOR(102),
    BRAILE_PANEL_ELEVATOR(103),
    ALLOWED_GUIDE_DOG(104),
    WHELLCHAIR_ADAPTED_WC(105),
    BRAILE_INFO(106);

    private final int value;

    Acessibility(int value) {
        this.value = value;
    }

    public int get() {
        return this.value;
    }


}
