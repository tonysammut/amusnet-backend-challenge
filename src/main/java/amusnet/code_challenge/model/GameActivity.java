package amusnet.code_challenge.model;

import lombok.Data;

@Data
public class GameActivity {
    private Long id;
    private Long playerId;
    private Double betAmount;
    private Double winAmount;
    private String currency;

    /**
     * Calculate the Gross Gaming Revenue for a player
     * 
     * @return the GRR for the player
     */
    public Double getGRR() {
        return betAmount - winAmount;
    }
}
