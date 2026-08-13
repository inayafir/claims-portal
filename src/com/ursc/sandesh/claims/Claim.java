package com.ursc.sandesh.claims;

/**
 * Model class representing a single claim record.
 * Maps to the CHSS_CLAIMS table.
 */
public class Claim {

    private int claimId;
    private String serialNumber;
    private String employeeStatus;
    private String staffNumber;
    private String employeeName;
    private double claimedAmount;
    private String meetingNumber;
    private String meetingDate;
    private String approvalStatus;
    private Double passedAmount;
    private String finalStatus;
    private String unpaidReason;
    private String createdAt;
    private String updatedAt;

    public Claim() {}

    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getEmployeeStatus() { return employeeStatus; }
    public void setEmployeeStatus(String employeeStatus) { this.employeeStatus = employeeStatus; }

    public String getStaffNumber() { return staffNumber; }
    public void setStaffNumber(String staffNumber) { this.staffNumber = staffNumber; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public double getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(double claimedAmount) { this.claimedAmount = claimedAmount; }

    public String getMeetingNumber() { return meetingNumber; }
    public void setMeetingNumber(String meetingNumber) { this.meetingNumber = meetingNumber; }

    public String getMeetingDate() { return meetingDate; }
    public void setMeetingDate(String meetingDate) { this.meetingDate = meetingDate; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public Double getPassedAmount() { return passedAmount; }
    public void setPassedAmount(Double passedAmount) { this.passedAmount = passedAmount; }

    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }

    public String getUnpaidReason() { return unpaidReason; }
    public void setUnpaidReason(String unpaidReason) { this.unpaidReason = unpaidReason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Returns the display status shown in the UI badge.
     * If Unpaid and unpaid_reason is set, shows the reason instead of "Unpaid".
     */
    public String getDisplayStatus() {
        if ("Unpaid".equals(finalStatus) && unpaidReason != null && !unpaidReason.isEmpty()) {
            return unpaidReason;
        }
        return finalStatus;
    }
}
