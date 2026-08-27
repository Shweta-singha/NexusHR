package org.Employee.service;

import org.Employee.audit.Auditable;
import org.Employee.dto.BulkReassignmentRequest;
import org.Employee.dto.BulkReassignmentResponse;
import org.Employee.dto.CreateDepartmentRequest;
import org.Employee.dto.DepartmentHierarchyResponse;
import org.Employee.dto.DepartmentResponse;
import org.Employee.dto.EmployeeOrgChartResponse;
import org.Employee.dto.OrgChartResponse;
import org.Employee.entity.Department;
import org.Employee.entity.Employee;
import org.Employee.repository.DepartmentRepository;
import org.Employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                  EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<DepartmentHierarchyResponse> getHierarchy() {
        List<Department> all = departmentRepository.findAll();
        Map<Long, Integer> empCountMap = buildEmpCountMap();
        Map<Long, List<Department>> childrenByParentId = groupChildrenByParent(all);

        return all.stream()
                .filter(d -> d.getParentDepartment() == null)
                .map(d -> buildTree(d, childrenByParentId, empCountMap))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public DepartmentHierarchyResponse getSubTree(Long departmentId) {
        Department root = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));

        List<Department> all = departmentRepository.findAll();
        Map<Long, Integer> empCountMap = buildEmpCountMap();
        Map<Long, List<Department>> childrenByParentId = groupChildrenByParent(all);

        return buildTree(root, childrenByParentId, empCountMap);
    }

    @Transactional
    @Override
    @Auditable(entityType = "DEPARTMENT", action = "CREATE")
    public DepartmentResponse create(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());

        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Parent department not found: " + request.getParentDepartmentId()));
            validateNoCircle(parent, department);
            department.setParentDepartment(parent);
        }

        Department saved = departmentRepository.save(department);
        return new DepartmentResponse(
                saved.getId(),
                saved.getName(),
                saved.getParentDepartment() == null ? null : saved.getParentDepartment().getId()
        );
    }

    @Transactional
    @Override
    @Auditable(entityType = "DEPARTMENT", action = "DELETE")
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));
        if (!department.getChildDepartments().isEmpty()) {
            throw new RuntimeException(
                    "Cannot delete department with sub-departments. Reassign or delete children first.");
        }
        if (employeeRepository.existsByDepartmentId(id)) {
            throw new RuntimeException(
                    "Cannot delete department with employees assigned. Reassign them first.");
        }
        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrgChartResponse> getOrgChart() {
        List<Department> all = departmentRepository.findAllWithEmployees();
        Map<Long, List<Department>> childrenMap = groupChildrenByParent(all);

        return all.stream()
                .filter(d -> d.getParentDepartment() == null)
                .map(d -> buildOrgChart(d, childrenMap))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public OrgChartResponse getDepartmentOrgChart(Long departmentId) {
        List<Department> all = departmentRepository.findAllWithEmployees();
        Map<Long, List<Department>> childrenMap = groupChildrenByParent(all);

        Department root = all.stream()
                .filter(d -> d.getId().equals(departmentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));

        return buildOrgChart(root, childrenMap);
    }

    @Transactional
    @Override
    @Auditable(entityType = "DEPARTMENT", action = "REASSIGN")
    public BulkReassignmentResponse reassignEmployees(BulkReassignmentRequest request) {
        // Deduplicate to avoid moving the same employee twice
        Set<Long> uniqueIds = new HashSet<>(request.getEmployeeIds());
        List<Long> distinctIds = new ArrayList<>(uniqueIds);

        Department target = departmentRepository.findById(request.getTargetDepartmentId())
                .orElseThrow(() -> new RuntimeException(
                        "Target department not found: " + request.getTargetDepartmentId()));

        List<Employee> employees = employeeRepository.findAllByEmployeeIdIn(distinctIds);

        if (employees.size() != distinctIds.size()) {
            throw new RuntimeException(
                    "One or more employees not found. Requested: " + distinctIds.size()
                    + ", found: " + employees.size());
        }

        for (Employee employee : employees) {
            employee.setDepartment(target);
        }
        employeeRepository.saveAll(employees);

        return new BulkReassignmentResponse(employees.size(), target.getId(), distinctIds);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<Long, Integer> buildEmpCountMap() {
        return departmentRepository.countEmployeesPerDepartment()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    private Map<Long, List<Department>> groupChildrenByParent(List<Department> all) {
        return all.stream()
                .filter(d -> d.getParentDepartment() != null)
                .collect(Collectors.groupingBy(d -> d.getParentDepartment().getId()));
    }

    private DepartmentHierarchyResponse buildTree(Department dept,
                                                   Map<Long, List<Department>> childrenMap,
                                                   Map<Long, Integer> empCountMap) {
        List<DepartmentHierarchyResponse> children = childrenMap
                .getOrDefault(dept.getId(), List.of())
                .stream()
                .map(child -> buildTree(child, childrenMap, empCountMap))
                .toList();

        int count = empCountMap.getOrDefault(dept.getId(), 0);
        return new DepartmentHierarchyResponse(dept.getId(), dept.getName(), count, children);
    }

    private OrgChartResponse buildOrgChart(Department dept, Map<Long, List<Department>> childrenMap) {
        List<EmployeeOrgChartResponse> employees = dept.getEmployees().stream()
                .map(e -> new EmployeeOrgChartResponse(e.getEmployeeId(), e.getUsername()))
                .toList();

        List<OrgChartResponse> children = childrenMap
                .getOrDefault(dept.getId(), List.of())
                .stream()
                .map(child -> buildOrgChart(child, childrenMap))
                .toList();

        return new OrgChartResponse(dept.getId(), dept.getName(), employees.size(), employees, children);
    }

    // Walks up the ancestor chain of `proposedParent` to ensure `child` doesn't already appear,
    // which would create a circular reference. Also guards against corrupt existing chains (depth > 50).
    private void validateNoCircle(Department proposedParent, Department child) {
        Department current = proposedParent;
        int depth = 0;
        while (current != null) {
            if (child.getId() != null && child.getId().equals(current.getId())) {
                throw new RuntimeException(
                        "Circular hierarchy detected: department '" + child.getName()
                        + "' is already an ancestor of '" + proposedParent.getName() + "'");
            }
            if (++depth > 50) {
                throw new RuntimeException("Department hierarchy exceeds maximum depth of 50");
            }
            current = current.getParentDepartment();
        }
    }
}
