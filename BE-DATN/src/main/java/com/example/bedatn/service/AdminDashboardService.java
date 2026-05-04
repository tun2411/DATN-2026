package com.example.bedatn.service;

import com.example.bedatn.documents.AssignmentBuildingEntity;
import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.documents.TransactionEntity;
import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.dto.response.AdminDashboardSummaryResponse;
import com.example.bedatn.dto.response.DashboardMetricComparisonResponse;
import com.example.bedatn.dto.response.RecentTransactionResponse;
import com.example.bedatn.enums.BuildingSaleStatus;
import com.example.bedatn.repository.AssignmentBuildingRepository;
import com.example.bedatn.repository.BuildingRepository;
import com.example.bedatn.repository.CustomerRepository;
import com.example.bedatn.repository.TransactionRepository;
import com.example.bedatn.repository.UserRepository;
import com.example.bedatn.security.MyUserDetail;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    private final BuildingRepository buildingRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AssignmentBuildingRepository assignmentBuildingRepository;

    public AdminDashboardService(BuildingRepository buildingRepository,
                                 CustomerRepository customerRepository,
                                 UserRepository userRepository,
                                 TransactionRepository transactionRepository,
                                 AssignmentBuildingRepository assignmentBuildingRepository) {
        this.buildingRepository = buildingRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.assignmentBuildingRepository = assignmentBuildingRepository;
    }

    public AdminDashboardSummaryResponse getSummary(MyUserDetail user) {
        YearMonth selectedMonth = YearMonth.now();
        return getSummary(user, selectedMonth.getYear(), selectedMonth.getMonthValue());
    }

    public AdminDashboardSummaryResponse getSummary(MyUserDetail user, Integer year, Integer month) {
        YearMonth selectedMonth = resolveMonth(year, month);
        if (hasRole(user, "MANAGER")) {
            return getManagerSummary(selectedMonth);
        }
        if (hasRole(user, "STAFF")) {
            return getStaffSummary(user.getId(), selectedMonth);
        }
        throw new IllegalArgumentException("Bạn không có quyền xem dashboard");
    }

    private AdminDashboardSummaryResponse getManagerSummary(YearMonth selectedMonth) {
        List<BuildingEntity> buildings = buildingRepository.findAll();
        List<TransactionEntity> transactions = transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        List<CustomerEntity> customers = customerRepository.findAll();
        List<UserEntity> users = userRepository.findAll().stream()
                .filter(user -> user.getStatus() == null || user.getStatus() != 0)
                .collect(Collectors.toList());
        AdminDashboardSummaryResponse response = buildMonthlySummary(selectedMonth, buildings, customers, users, transactions);
        return response;
    }

    private AdminDashboardSummaryResponse getStaffSummary(Long staffId, YearMonth selectedMonth) {
        List<BuildingEntity> buildings = buildingsForStaff(staffId);
        Set<Long> customerIds = buildings.stream()
                .map(BuildingEntity::getLinkedCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        List<CustomerEntity> customers = customerIds.isEmpty()
                ? Collections.emptyList()
                : customerRepository.findAllById(customerIds);
        List<TransactionEntity> transactions = customerIds.isEmpty()
                ? Collections.emptyList()
                : transactionRepository.findAllByCustomerIdIn(new ArrayList<>(customerIds), Sort.by(Sort.Direction.DESC, "createdDate"));
        return buildMonthlySummary(selectedMonth, buildings, customers, Collections.emptyList(), transactions);
    }

    private AdminDashboardSummaryResponse buildMonthlySummary(YearMonth selectedMonth,
                                                              List<BuildingEntity> buildings,
                                                              List<CustomerEntity> customers,
                                                              List<UserEntity> users,
                                                              List<TransactionEntity> transactions) {
        YearMonth previousMonth = selectedMonth.minusMonths(1);
        List<BuildingEntity> currentBuildings = buildings.stream()
                .filter(building -> isInMonth(building, selectedMonth))
                .collect(Collectors.toList());
        List<BuildingEntity> previousBuildings = buildings.stream()
                .filter(building -> isInMonth(building, previousMonth))
                .collect(Collectors.toList());
        List<CustomerEntity> currentCustomers = customers.stream()
                .filter(customer -> isInMonth(customer.getCreatedDate(), customer.getId(), selectedMonth))
                .collect(Collectors.toList());
        List<CustomerEntity> previousCustomers = customers.stream()
                .filter(customer -> isInMonth(customer.getCreatedDate(), customer.getId(), previousMonth))
                .collect(Collectors.toList());
        List<UserEntity> currentUsers = users.stream()
                .filter(user -> isInMonth(user.getCreatedDate(), user.getId(), selectedMonth))
                .collect(Collectors.toList());
        List<UserEntity> previousUsers = users.stream()
                .filter(user -> isInMonth(user.getCreatedDate(), user.getId(), previousMonth))
                .collect(Collectors.toList());
        List<TransactionEntity> currentTransactions = transactions.stream()
                .filter(transaction -> isInMonth(transaction.getCreatedDate(), transaction.getId(), selectedMonth))
                .collect(Collectors.toList());
        List<TransactionEntity> previousTransactions = transactions.stream()
                .filter(transaction -> isInMonth(transaction.getCreatedDate(), transaction.getId(), previousMonth))
                .collect(Collectors.toList());

        AdminDashboardSummaryResponse response = new AdminDashboardSummaryResponse();
        response.setYear(selectedMonth.getYear());
        response.setMonth(selectedMonth.getMonthValue());
        response.setPreviousYear(previousMonth.getYear());
        response.setPreviousMonth(previousMonth.getMonthValue());
        response.setTotalBuildings(currentBuildings.size());
        response.setTotalCustomers(currentCustomers.size());
        response.setTotalAccounts(currentUsers.size());
        response.setTotalTransactions(currentTransactions.size());
        response.setSoldRevenue(calculateSoldRevenue(currentBuildings));
        response.setBuildingsByStatus(groupBuildingsByStatus(currentBuildings));
        response.setTransactionsByStatus(groupTransactionsByStatus(currentTransactions));
        response.setComparisons(buildComparisons(
                currentBuildings,
                previousBuildings,
                currentCustomers,
                previousCustomers,
                currentUsers,
                previousUsers,
                currentTransactions,
                previousTransactions));
        response.setRecentTransactions(toRecentResponses(currentTransactions.stream()
                .filter(this::isSaleActivity)
                .sorted(Comparator.comparing(this::effectiveTransactionDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList())));
        return response;
    }

    private Map<String, DashboardMetricComparisonResponse> buildComparisons(List<BuildingEntity> currentBuildings,
                                                                            List<BuildingEntity> previousBuildings,
                                                                            List<CustomerEntity> currentCustomers,
                                                                            List<CustomerEntity> previousCustomers,
                                                                            List<UserEntity> currentUsers,
                                                                            List<UserEntity> previousUsers,
                                                                            List<TransactionEntity> currentTransactions,
                                                                            List<TransactionEntity> previousTransactions) {
        Map<String, DashboardMetricComparisonResponse> comparisons = new LinkedHashMap<>();
        comparisons.put("totalBuildings", DashboardMetricComparisonResponse.of(currentBuildings.size(), previousBuildings.size()));
        comparisons.put("totalCustomers", DashboardMetricComparisonResponse.of(currentCustomers.size(), previousCustomers.size()));
        comparisons.put("soldRevenue", DashboardMetricComparisonResponse.of(calculateSoldRevenue(currentBuildings), calculateSoldRevenue(previousBuildings)));
        comparisons.put("totalAccounts", DashboardMetricComparisonResponse.of(currentUsers.size(), previousUsers.size()));
        comparisons.put("totalTransactions", DashboardMetricComparisonResponse.of(currentTransactions.size(), previousTransactions.size()));
        return comparisons;
    }

    private List<BuildingEntity> buildingsForStaff(Long staffId) {
        if (staffId == null) {
            return Collections.emptyList();
        }
        List<Long> buildingIds = assignmentBuildingRepository.findByStaffId(staffId).stream()
                .map(AssignmentBuildingEntity::getBuildingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (buildingIds.isEmpty()) {
            return Collections.emptyList();
        }
        return buildingRepository.findByIdIn(buildingIds);
    }

    private BigDecimal calculateSoldRevenue(List<BuildingEntity> buildings) {
        return buildings.stream()
                .filter(building -> BuildingSaleStatus.SOLD.name().equalsIgnoreCase(safeStatus(building.getSaleStatus())))
                .map(BuildingEntity::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, Long> groupBuildingsByStatus(List<BuildingEntity> buildings) {
        Map<String, Long> grouped = buildings.stream()
                .collect(Collectors.groupingBy(building -> normalizeKey(building.getSaleStatus()), LinkedHashMap::new, Collectors.counting()));
        for (BuildingSaleStatus status : BuildingSaleStatus.values()) {
            grouped.putIfAbsent(status.name(), 0L);
        }
        grouped.putIfAbsent("UNKNOWN", 0L);
        return grouped;
    }

    private Map<String, Long> groupTransactionsByStatus(List<TransactionEntity> transactions) {
        Map<String, Long> grouped = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> transaction.getStatus() == null ? "UNKNOWN" : transaction.getStatus().name(), LinkedHashMap::new, Collectors.counting()));
        grouped.putIfAbsent("PENDING", 0L);
        grouped.putIfAbsent("PROCESSING", 0L);
        grouped.putIfAbsent("SUCCESS", 0L);
        grouped.putIfAbsent("CANCELLED", 0L);
        grouped.putIfAbsent("UNKNOWN", 0L);
        return grouped;
    }

    private List<RecentTransactionResponse> toRecentResponses(List<TransactionEntity> transactions) {
        Set<Long> customerIds = transactions.stream()
                .map(TransactionEntity::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> buildingIds = transactions.stream()
                .map(TransactionEntity::getBuildingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, CustomerEntity> customersById = customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(CustomerEntity::getId, Function.identity()));
        Map<Long, BuildingEntity> buildingsById = buildingRepository.findAllById(buildingIds).stream()
                .collect(Collectors.toMap(BuildingEntity::getId, Function.identity()));

        return transactions.stream()
                .sorted(Comparator.comparing(this::effectiveTransactionDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(transaction -> toRecentResponse(transaction, customersById, buildingsById))
                .collect(Collectors.toList());
    }

    private boolean isSaleActivity(TransactionEntity transaction) {
        if (transaction == null || transaction.getTransactionType() == null) {
            return false;
        }
        String type = transaction.getTransactionType().name();
        return "DEPOSIT".equals(type) || "SALE".equals(type);
    }

    private RecentTransactionResponse toRecentResponse(TransactionEntity transaction,
                                                       Map<Long, CustomerEntity> customersById,
                                                       Map<Long, BuildingEntity> buildingsById) {
        RecentTransactionResponse response = new RecentTransactionResponse();
        response.setId(transaction.getId());
        response.setCode(transaction.getCode());
        response.setNote(transaction.getNote());
        response.setCustomerId(transaction.getCustomerId());
        response.setStaffId(transaction.getStaffId());
        response.setBuildingId(transaction.getBuildingId());
        response.setTransactionType(transaction.getTransactionType());
        response.setStatus(transaction.getStatus());
        response.setAmount(transaction.getAmount());
        response.setCreatedDate(effectiveTransactionDate(transaction));

        CustomerEntity customer = customersById.get(transaction.getCustomerId());
        if (customer != null) {
            response.setCustomerName(customer.getFullName());
        }
        BuildingEntity building = buildingsById.get(transaction.getBuildingId());
        if (building != null) {
            response.setBuildingName(building.getName());
        }
        return response;
    }

    private String normalizeKey(String value) {
        String normalized = safeStatus(value);
        return normalized.isEmpty() ? "UNKNOWN" : normalized.toUpperCase();
    }

    private String safeStatus(String value) {
        return value == null ? "" : value.trim();
    }

    private YearMonth resolveMonth(Integer year, Integer month) {
        YearMonth now = YearMonth.now();
        int resolvedYear = year == null ? now.getYear() : year;
        int resolvedMonth = month == null ? now.getMonthValue() : month;
        if (resolvedMonth < 1 || resolvedMonth > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        return YearMonth.of(resolvedYear, resolvedMonth);
    }

    private boolean isInMonth(BuildingEntity building, YearMonth month) {
        if (building.getCreatedDate() != null) {
            return isInMonth(building.getCreatedDate(), month);
        }
        LocalDateTime createdAt = building.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        return YearMonth.from(createdAt).equals(month);
    }

    private boolean isInMonth(Date date, YearMonth month) {
        if (date == null) {
            return false;
        }
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return YearMonth.from(localDate).equals(month);
    }

    private boolean isInMonth(Date date, Long id, YearMonth month) {
        if (date != null) {
            return isInMonth(date, month);
        }
        if (id == null) {
            return false;
        }
        try {
            Date fallbackDate = new Date(id);
            return isInMonth(fallbackDate, month);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private Date effectiveTransactionDate(TransactionEntity transaction) {
        if (transaction == null) {
            return null;
        }
        if (transaction.getCreatedDate() != null) {
            return transaction.getCreatedDate();
        }
        Long id = transaction.getId();
        if (id == null) {
            return null;
        }
        try {
            return new Date(id);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private boolean hasRole(MyUserDetail user, String roleWithoutPrefix) {
        if (user == null) {
            return false;
        }
        String want = "ROLE_" + roleWithoutPrefix;
        Collection<GrantedAuthority> authorities = user.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (want.equalsIgnoreCase(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
