package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
public class DashboardMetricComparisonResponse {
    private BigDecimal currentValue = BigDecimal.ZERO;
    private BigDecimal previousValue = BigDecimal.ZERO;
    private BigDecimal difference = BigDecimal.ZERO;
    private BigDecimal percentChange = BigDecimal.ZERO;
    private String direction = "FLAT";

    public static DashboardMetricComparisonResponse of(Number current, Number previous) {
        BigDecimal currentValue = toBigDecimal(current);
        BigDecimal previousValue = toBigDecimal(previous);
        DashboardMetricComparisonResponse response = new DashboardMetricComparisonResponse();
        response.setCurrentValue(currentValue);
        response.setPreviousValue(previousValue);
        response.setDifference(currentValue.subtract(previousValue));
        if (previousValue.compareTo(BigDecimal.ZERO) == 0) {
            response.setPercentChange(currentValue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100));
        } else {
            response.setPercentChange(response.getDifference()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previousValue.abs(), 2, RoundingMode.HALF_UP));
        }
        if (response.getDifference().compareTo(BigDecimal.ZERO) > 0) {
            response.setDirection("UP");
        } else if (response.getDifference().compareTo(BigDecimal.ZERO) < 0) {
            response.setDirection("DOWN");
        }
        return response;
    }

    private static BigDecimal toBigDecimal(Number value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(value.doubleValue());
    }
}
