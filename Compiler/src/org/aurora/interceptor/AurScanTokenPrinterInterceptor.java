package org.aurora.interceptor;

import org.aurora.scanner.ScannedData;
import org.aurora.scanner.Token;
import org.aurora.util.AurFile;

public class AurScanTokenPrinterInterceptor implements AurPassiveInterceptor<AurFile, ScannedData> {

    @Override
    public void beforeState(AurFile input) {
    }

    @Override
    public void afterState(ScannedData input) {
        for(Token token : input.getTokens()) {
            System.out.println(token);
        }
    }
}
