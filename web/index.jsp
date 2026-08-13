<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Claims Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <header>
        <div class="header-content">
            <h1>Claims Portal</h1>
            <div class="header-actions">
                <button class="btn btn-add" onclick="openModal()">+ Add Claim</button>
                <button class="btn btn-import" onclick="document.getElementById('excelFile').click()">Import Excel</button>
                <button class="btn btn-rollback" id="rollbackBtn" onclick="rollback()" disabled>Rollback</button>
                <button class="btn btn-export" onclick="exportExcel()">Export Excel</button>
            </div>
        </div>
    </header>

    <main>
        <section class="dashboard">
            <div class="stat-card stat-total">
                <div class="stat-label">Total Bills</div>
                <div class="stat-value" id="stat-total">0</div>
            </div>
            <div class="stat-card stat-paid">
                <div class="stat-label">Paid</div>
                <div class="stat-value" id="stat-paid">0</div>
            </div>
            <div class="stat-card stat-cmo">
                <div class="stat-label">CMO Clarification</div>
                <div class="stat-value" id="stat-cmo">0</div>
            </div>
            <div class="stat-card stat-objection">
                <div class="stat-label">Objection</div>
                <div class="stat-value" id="stat-objection">0</div>
            </div>
            <div class="stat-card stat-high">
                <div class="stat-label">High Value</div>
                <div class="stat-value" id="stat-high">0</div>
            </div>
        </section>

        <section class="filters">
            <div class="filter-row">
                <div class="filter-group">
                    <label>Search by Staff Number</label>
                    <input type="text" id="searchInput" placeholder="IS0xxxx..." oninput="debounceSearch()">
                </div>
                <div class="filter-group">
                    <label>Claim Status</label>
                    <select id="statusFilter" onchange="loadClaims()">
                        <option value="All">All</option>
                        <option value="Paid">Paid</option>
                        <option value="Objection Exists">Objection Exists</option>
                        <option value="CMO Clarification">CMO Clarification</option>
                        <option value="High Value">High Value</option>
                        <option value="NIL">NIL</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label>Employee Status</label>
                    <select id="employeeFilter" onchange="loadClaims()">
                        <option value="All">All</option>
                        <option value="Serving">Serving</option>
                        <option value="Retired">Retired</option>
                        <option value="CISF">CISF</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label>From Date</label>
                    <input type="date" id="dateFrom" onchange="loadClaims()">
                </div>
                <div class="filter-group">
                    <label>To Date</label>
                    <input type="date" id="dateTo" onchange="loadClaims()">
                </div>
                <div class="filter-group filter-actions">
                    <button class="btn btn-clear" onclick="clearFilters()">Clear Filters</button>
                </div>
            </div>
        </section>

        <section class="table-section">
            <table id="claimsTable">
                <thead>
                    <tr>
                        <th>Serial No.</th>
                        <th>Emp. Status</th>
                        <th>Staff Number</th>
                        <th>Name</th>
                        <th>Claimed Amount</th>
                        <th>Meeting No.</th>
                        <th>Meeting Date</th>
                        <th>Approval</th>
                        <th>Passed Amount</th>
                        <th>Final Status</th>
                        <th>Unpaid Reason</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody id="claimsBody"></tbody>
            </table>
            <div id="noResults" class="no-results" style="display:none;">No claims found.</div>
        </section>
    </main>

    <input type="file" id="excelFile" accept=".xlsx" style="display:none;" onchange="handleImport(this)">

    <div class="modal-overlay" id="modalOverlay" onclick="closeModal()">
        <div class="modal" onclick="event.stopPropagation()">
            <div class="modal-header">
                <h2 id="modalTitle">Add Claim</h2>
                <button class="modal-close" onclick="closeModal()">&times;</button>
            </div>
            <form id="claimForm" onsubmit="handleSubmit(event)">
                <input type="hidden" id="claimId">
                <div class="form-grid">
                    <div class="form-group">
                        <label>Serial Number *</label>
                        <input type="text" id="serialNumber" required>
                    </div>
                    <div class="form-group">
                        <label>Employee Status *</label>
                        <select id="servingRetired" required>
                            <option value="Serving">Serving</option>
                            <option value="Retired">Retired</option>
                            <option value="CISF">CISF</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Staff Number *</label>
                        <input type="text" id="staffNumber" required placeholder="IS0xxxx"
                               onblur="lookupEmployee(this.value)">
                    </div>
                    <div class="form-group">
                        <label>Name *</label>
                        <input type="text" id="claimName" required>
                    </div>
                    <div class="form-group">
                        <label>Claimed Amount *</label>
                        <input type="number" step="0.01" id="claimedAmount" required>
                    </div>
                    <div class="form-group">
                        <label>Meeting Number</label>
                        <input type="text" id="meetingNumber">
                    </div>
                    <div class="form-group">
                        <label>Meeting Date</label>
                        <input type="date" id="meetingDate">
                    </div>
                    <div class="form-group">
                        <label>Approval Status *</label>
                        <select id="approvalStatus" required>
                            <option value="Approved">Approved</option>
                            <option value="Not Approved">Not Approved</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Passed Amount</label>
                        <input type="number" step="0.01" id="passedAmount">
                    </div>
                    <div class="form-group">
                        <label>Final Status</label>
                        <select id="finalStatus" onchange="toggleUnpaidReason()">
                            <option value="Unpaid">Unpaid</option>
                            <option value="Paid">Paid</option>
                        </select>
                    </div>
                    <div class="form-group" id="unpaidReasonGroup">
                        <label>Unpaid Reason</label>
                        <select id="unpaidReason">
                            <option value="">Select...</option>
                            <option value="CMO Clarification">CMO Clarification</option>
                            <option value="Objection Exists">Objection Exists</option>
                            <option value="High Value">High Value</option>
                            <option value="NIL">NIL</option>
                        </select>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-cancel" onclick="closeModal()">Cancel</button>
                    <button type="submit" class="btn btn-save" id="saveBtn">Save</button>
                </div>
            </form>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/script.js"></script>
</body>
</html>
