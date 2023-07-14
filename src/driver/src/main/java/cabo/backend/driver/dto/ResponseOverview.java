package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseOverview {

    private long totalTrip;

    private double averageIncomePerDrive;

    private ResponseRecentTrip recentTrip;
}
