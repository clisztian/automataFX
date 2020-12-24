package tradingeconomics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representation of daily adjusted response from api.
 *
 * @see TimeSeriesResponse
 */
public class DailyAdjusted extends TimeSeriesResponse {

  private DailyAdjusted(final Map<String, String> metaData,
                        final List<StockData> stocks) {
    super(metaData, stocks);
  }

  /**
   * Creates {@code DailyAdjusted} instance from json.
   *
   * @param json string to parse
   * @return DailyAdjusted instance
   */
  public static DailyAdjusted from(String json)  {
    Parser parser = new Parser();
    return parser.parseJson(json);
  }

  /**
   * Helper class for parsing json to {@code DailyAdjusted}.
   *
   * @see TimeSeriesParser
   * @see JsonParser
   */
  private static class Parser extends TimeSeriesParser<DailyAdjusted> {

    @Override
    String getStockDataKey() {
      return "Time Series (Daily)";
    }

    @Override
    DailyAdjusted resolve(Map<String, String> metaData,
                          Map<String, Map<String, String>> stockData)  {
      List<StockData> stocks = new ArrayList<>();
      try {
        stockData.forEach((key, values) -> stocks.add(new StockData(
                LocalDate.parse(key, SIMPLE_DATE_FORMAT).atStartOfDay(),
                Double.parseDouble(values.get("Open")),
                Double.parseDouble(values.get("High")),
                Double.parseDouble(values.get("Low")),
                Double.parseDouble(values.get("Close"))

        )));
      } catch (Exception e) {
        throw new AlphaVantageException("Daily adjusted api change", e);
      }
      return new DailyAdjusted(metaData, stocks);
    }
  }
}