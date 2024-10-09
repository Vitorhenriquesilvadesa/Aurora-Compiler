package org.aurora.interceptor;

import org.aurora.scanner.AurScannedData;
import org.aurora.scanner.Token;
import org.aurora.util.AurFile;

public class AurScanTokenPrinterInterceptor implements AurPassiveInterceptor<AurFile, AurScannedData> {

    @Override
    public void beforeState(AurFile input) {
    }

    @Override
    public void afterState(AurScannedData input) {
        for(Token token : input.getTokens()) {
            System.out.println(token);
        }
    }
}
