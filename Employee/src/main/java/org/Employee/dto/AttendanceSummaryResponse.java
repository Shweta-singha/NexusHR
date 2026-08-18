package org.Employee.dto;

public class AttendanceSummaryResponse {

    private Long presentToday;
    private Long checkedIn;
    private Long checkedOut;
    private Long overtimeEmployees;

    public AttendanceSummaryResponse() {}

    public AttendanceSummaryResponse(
            Long presentToday,
            Long checkedIn,
            Long checkedOut,
            Long overtimeEmployees) {
        this.presentToday = presentToday;
        this.checkedIn = checkedIn;
        this.checkedOut = checkedOut;
        this.overtimeEmployees = overtimeEmployees;
    }

    public Long getPresentToday() { return presentToday; }
    public void setPresentToday(Long presentToday) { this.presentToday = presentToday; }

    public Long getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Long checkedIn) { this.checkedIn = checkedIn; }

    public Long getCheckedOut() { return checkedOut; }
    public void setCheckedOut(Long checkedOut) { this.checkedOut = checkedOut; }

    public Long getOvertimeEmployees() { return overtimeEmployees; }
    public void setOvertimeEmployees(Long overtimeEmployees) { this.overtimeEmployees = overtimeEmployees; }
}
