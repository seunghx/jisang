package com.jisang.security.exception;

import java.util.Locale;

public class SecurityUnsupportedLocaleException extends SecurityBadRequestException {

    // Static Fiedls
    // ==========================================================================================================================

    private static final long serialVersionUID = -8512723981080667609L;
    private static final String LOCALE_FIELD = "locale";

    // Instance Fields
    // ==========================================================================================================================

    private Locale locale;

    // Constructors
    // ==========================================================================================================================

    public SecurityUnsupportedLocaleException(String msg) {
        super(msg);
    }

    public SecurityUnsupportedLocaleException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SecurityUnsupportedLocaleException(String msg, Throwable cause, Locale locale) {
        super(msg, cause);
        this.locale = locale;
    }

    // Methods 
    // ==========================================================================================================================

    public String getLocaleField() {
        return LOCALE_FIELD;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return super.toString() + "[unsupported locale=" + locale + "]";
    }
}
