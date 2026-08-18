package org.Employee.dto;

import java.math.BigDecimal;

public class LeaveBalanceResponse {

    private Integer casual;
    private Integer sick;
    private BigDecimal earned;
    private Integer compOff;

    public LeaveBalanceResponse() {}

    public LeaveBalanceResponse(Integer casual, Integer sick, BigDecimal earned, Integer compOff) {
        this.casual = casual;
        this.sick = sick;
        this.earned = earned;
        this.compOff = compOff;
    }

    public Integer getCasual() { return casual; }
    public void setCasual(Integer casual) { this.casual = casual; }

    public Integer getSick() { return sick; }
    public void setSick(Integer sick) { this.sick = sick; }

    public BigDecimal getEarned() { return earned; }
    public void setEarned(BigDecimal earned) { this.earned = earned; }

    public Integer getCompOff() { return compOff; }
    public void setCompOff(Integer compOff) { this.compOff = compOff; }
}
