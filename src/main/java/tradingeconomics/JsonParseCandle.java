package tradingeconomics;

import java.util.List;

public class JsonParseCandle {

    private List<Candle> results;

    public JsonParseCandle(List<Candle> Candles) {
        super();
        this.results = Candles;
    }

    public List<Candle> getCandle() {
        return results;
    }

    public void setCandle(List<Candle> results) {
        this.results = results;
    }

}
