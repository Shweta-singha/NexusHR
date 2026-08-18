package org.Employee.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.PayrollStatus;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PayslipServiceImpl implements PayslipService {

    private final PayrollRepository payrollRepository;
    private final SalaryStructureRepository salaryStructureRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePayslip(Long payrollId) {

        PayrollRecord payroll = payrollRepository
                .findById(payrollId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payroll not found"));

        if (payroll.getStatus() == PayrollStatus.DRAFT) {
            throw new IllegalStateException(
                    "Payslip cannot be generated for DRAFT payroll");
        }

        SalaryStructure salary = salaryStructureRepository
                .findByEmployeeEmployeeId(payroll.getEmployee().getEmployeeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Salary structure not found"));

        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);

            PdfWriter.getInstance(document, outputStream);

            document.open();

            // ---------------------------------------------------------
            // COMPANY HEADER
            // ---------------------------------------------------------

            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);

            Paragraph company = new Paragraph("NexusHR", companyFont);
            company.setAlignment(Element.ALIGN_CENTER);
            document.add(company);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

            Paragraph title = new Paragraph("PAYSLIP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // ---------------------------------------------------------
            // EMPLOYEE DETAILS
            // ---------------------------------------------------------

            PdfPTable employeeTable = new PdfPTable(2);
            employeeTable.setWidthPercentage(100);

            addRow(employeeTable, "Employee ID",
                    String.valueOf(payroll.getEmployee().getEmployeeId()));
            addRow(employeeTable, "Employee Name", payroll.getEmployee().getUsername());
            addRow(employeeTable, "Payroll Month", payroll.getPayrollMonth());
            addRow(employeeTable, "Payroll Status", payroll.getStatus().name());

            document.add(employeeTable);
            document.add(new Paragraph(" "));

            // ---------------------------------------------------------
            // EARNINGS
            // ---------------------------------------------------------

            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);

            document.add(new Paragraph("EARNINGS", sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable earningsTable = new PdfPTable(2);
            earningsTable.setWidthPercentage(100);

            addHeader(earningsTable, "Component", "Amount");

            addRow(earningsTable, "Basic Pay", money(salary.getBasicPay()));
            addRow(earningsTable, "HRA", money(salary.getHra()));
            addRow(earningsTable, "Special Allowance", money(salary.getSpecialAllowance()));
            addRow(earningsTable, "Conveyance Allowance", money(salary.getConveyanceAllowance()));
            addRow(earningsTable, "Medical Allowance", money(salary.getMedicalAllowance()));
            addRow(earningsTable, "Bonus", money(salary.getBonus()));
            addRow(earningsTable, "Gross Salary", money(payroll.getGrossSalary()));

            document.add(earningsTable);
            document.add(new Paragraph(" "));

            // ---------------------------------------------------------
            // DEDUCTIONS
            // ---------------------------------------------------------

            document.add(new Paragraph("DEDUCTIONS", sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable deductionTable = new PdfPTable(2);
            deductionTable.setWidthPercentage(100);

            addHeader(deductionTable, "Deduction", "Amount");

            addRow(deductionTable, "Provident Fund (PF)", money(payroll.getPfDeduction()));
            addRow(deductionTable, "ESI", money(payroll.getEsiDeduction()));
            addRow(deductionTable, "Income Tax", money(payroll.getTaxDeduction()));

            double totalDeductions =
                    safe(payroll.getPfDeduction())
                    + safe(payroll.getEsiDeduction())
                    + safe(payroll.getTaxDeduction());

            addRow(deductionTable, "Total Deductions", money(totalDeductions));

            document.add(deductionTable);
            document.add(new Paragraph(" "));

            // ---------------------------------------------------------
            // NET SALARY
            // ---------------------------------------------------------

            Font netFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);

            Paragraph netSalary = new Paragraph(
                    "NET SALARY: " + money(payroll.getNetSalary()), netFont);
            netSalary.setAlignment(Element.ALIGN_RIGHT);
            document.add(netSalary);

            document.add(new Paragraph(" "));

            // ---------------------------------------------------------
            // FOOTER
            // ---------------------------------------------------------

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);

            Paragraph footer = new Paragraph(
                    "This is a system-generated payslip.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate payslip PDF", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value) {

        PdfPCell labelCell = new PdfPCell(new Phrase(label));
        PdfPCell valueCell = new PdfPCell(new Phrase(value));

        labelCell.setPadding(8);
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeader(PdfPTable table, String left, String right) {

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        PdfPCell leftCell = new PdfPCell(new Phrase(left, font));
        PdfPCell rightCell = new PdfPCell(new Phrase(right, font));

        leftCell.setPadding(8);
        rightCell.setPadding(8);

        table.addCell(leftCell);
        table.addCell(rightCell);
    }

    private String money(Double amount) {
        return String.format("INR %.2f", safe(amount));
    }

    private double safe(Double amount) {
        return amount == null ? 0.0 : amount;
    }
}
