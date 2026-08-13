// =================================================================
// Claims Portal - Client-side JavaScript
// All fetch() calls point to /claims servlet with action parameter
// =================================================================

var searchTimeout = null;
var requestCounter = 0;
var CONTEXT_PATH = "";

// Determine context path from current URL
(function() {
    var path = window.location.pathname;
    // If path ends with /claims, strip the last segment
    // Otherwise use everything up to the last /
    var lastSlash = path.lastIndexOf("/");
    if (lastSlash > 0) {
        CONTEXT_PATH = path.substring(0, lastSlash);
    }
})();

// =================================================================
// DEBOUNCED SEARCH
// =================================================================
function debounceSearch() {
    if (searchTimeout) clearTimeout(searchTimeout);
    searchTimeout = setTimeout(loadClaims, 300);
}

// =================================================================
// LOAD DASHBOARD STATS
// =================================================================
function loadDashboard() {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", CONTEXT_PATH + "/claims?action=dashboard", true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            try {
                var data = JSON.parse(xhr.responseText);
                document.getElementById("stat-total").textContent = data.total || 0;
                document.getElementById("stat-paid").textContent = data.paid || 0;
                document.getElementById("stat-cmo").textContent = data.cmo_clarification || 0;
                document.getElementById("stat-objection").textContent = data.objection_exists || 0;
                document.getElementById("stat-high").textContent = data.high_value || 0;
            } catch (e) {}
        }
    };
    xhr.send();
}

// =================================================================
// LOAD CLAIMS (with filters)
// =================================================================
function loadClaims() {
    var params = [];
    var search = document.getElementById("searchInput").value.trim();
    var status = document.getElementById("statusFilter").value;
    var employee = document.getElementById("employeeFilter").value;
    var dateFrom = document.getElementById("dateFrom").value;
    var dateTo = document.getElementById("dateTo").value;

    if (search) params.push("search=" + encodeURIComponent(search));
    if (status !== "All") params.push("status=" + encodeURIComponent(status));
    if (employee !== "All") params.push("employee=" + encodeURIComponent(employee));
    if (dateFrom) params.push("date_from=" + encodeURIComponent(dateFrom));
    if (dateTo) params.push("date_to=" + encodeURIComponent(dateTo));

    var url = CONTEXT_PATH + "/claims?action=list";
    if (params.length > 0) url += "&" + params.join("&");

    requestCounter++;
    var currentReq = requestCounter;

    var xhr = new XMLHttpRequest();
    xhr.open("GET", url, true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (currentReq !== requestCounter) return;
            if (xhr.status === 200) {
                try {
                    var claims = JSON.parse(xhr.responseText);
                    renderClaims(claims);
                    loadDashboard();
                } catch (e) {}
            }
        }
    };
    xhr.send();
}

// =================================================================
// RENDER CLAIMS TABLE
// =================================================================
function renderClaims(claims) {
    var tbody = document.getElementById("claimsBody");
    var noResults = document.getElementById("noResults");

    if (!claims || claims.length === 0) {
        tbody.innerHTML = "";
        noResults.style.display = "block";
        return;
    }
    noResults.style.display = "none";

    var html = "";
    for (var i = 0; i < claims.length; i++) {
        var claim = claims[i];
        var displayStatus = (claim.final_status === "Unpaid" && claim.unpaid_reason)
            ? claim.unpaid_reason
            : claim.final_status;

        var statusClass = getStatusClass(displayStatus);
        var rowClass = claim.final_status === "Paid" ? "row-paid" : "";

        html += '<tr class="' + rowClass + '">'
            + '<td>' + esc(claim.serial_number) + '</td>'
            + '<td>' + esc(claim.serving_retired) + '</td>'
            + '<td>' + esc(claim.staff_number) + '</td>'
            + '<td>' + esc(claim.name) + '</td>'
            + '<td>' + formatMoney(claim.claimed_amount) + '</td>'
            + '<td>' + esc(claim.meeting_number || '-') + '</td>'
            + '<td>' + esc(claim.meeting_date || '-') + '</td>'
            + '<td>' + esc(claim.approval_status) + '</td>'
            + '<td>' + (claim.passed_amount != null ? formatMoney(claim.passed_amount) : '-') + '</td>'
            + '<td><span class="status-badge ' + statusClass + '">' + esc(displayStatus) + '</span></td>'
            + '<td>' + esc(claim.unpaid_reason || '-') + '</td>'
            + '<td>'
            + '<button class="btn btn-edit" onclick="editClaim(' + claim.id + ')">Edit</button> '
            + '<button class="btn btn-delete" onclick="deleteClaim(' + claim.id + ')">Delete</button>'
            + '</td>'
            + '</tr>';
    }
    tbody.innerHTML = html;
}

// =================================================================
// STATUS CLASS MAPPING
// =================================================================
function getStatusClass(status) {
    switch (status) {
        case "Paid": return "status-paid";
        case "Objection Exists": return "status-objection";
        case "CMO Clarification": return "status-cmo";
        case "High Value": return "status-high";
        case "NIL": return "status-nil";
        default: return "";
    }
}

// =================================================================
// FORMAT HELPERS
// =================================================================
function formatMoney(val) {
    if (val == null) return "-";
    return Number(val).toLocaleString("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function esc(str) {
    if (str == null) return "";
    var div = document.createElement("div");
    div.appendChild(document.createTextNode(String(str)));
    return div.innerHTML;
}

// =================================================================
// MODAL: OPEN / CLOSE / TOGGLE
// =================================================================
function openModal(claim) {
    document.getElementById("modalOverlay").classList.add("active");
    document.getElementById("claimForm").reset();
    document.getElementById("claimId").value = "";

    if (claim) {
        document.getElementById("modalTitle").textContent = "Edit Claim";
        document.getElementById("claimId").value = claim.id;
        document.getElementById("serialNumber").value = claim.serial_number || "";
        document.getElementById("servingRetired").value = claim.serving_retired || "Serving";
        document.getElementById("staffNumber").value = claim.staff_number || "";
        document.getElementById("claimName").value = claim.name || "";
        document.getElementById("claimedAmount").value = claim.claimed_amount || "";
        document.getElementById("meetingNumber").value = claim.meeting_number || "";
        document.getElementById("meetingDate").value = claim.meeting_date || "";
        document.getElementById("approvalStatus").value = claim.approval_status || "Approved";
        document.getElementById("passedAmount").value = claim.passed_amount != null ? claim.passed_amount : "";
        document.getElementById("finalStatus").value = claim.final_status || "Unpaid";
        document.getElementById("unpaidReason").value = claim.unpaid_reason || "";
    } else {
        document.getElementById("modalTitle").textContent = "Add Claim";
    }
    toggleUnpaidReason();
    document.getElementById("saveBtn").disabled = false;
    document.getElementById("saveBtn").textContent = "Save";
}

function closeModal() {
    document.getElementById("modalOverlay").classList.remove("active");
}

function toggleUnpaidReason() {
    var showPaid = document.getElementById("finalStatus").value === "Paid";
    document.getElementById("unpaidReasonGroup").style.display = showPaid ? "none" : "flex";
}

// =================================================================
// EMPLOYEE LOOKUP (on staff number blur)
// =================================================================
function lookupEmployee(staffNumber) {
    if (!staffNumber || !staffNumber.trim()) return;

    var xhr = new XMLHttpRequest();
    xhr.open("GET", CONTEXT_PATH + "/claims?action=lookup-employee&staff_number=" + encodeURIComponent(staffNumber.trim()), true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            try {
                var data = JSON.parse(xhr.responseText);
                if (data.found && data.name) {
                    document.getElementById("claimName").value = data.name;
                }
            } catch (e) {}
        }
    };
    xhr.send();
}

// =================================================================
// FORM SUBMIT: CREATE or UPDATE
// =================================================================
function handleSubmit(e) {
    e.preventDefault();
    var btn = document.getElementById("saveBtn");
    if (btn.disabled) return;
    btn.disabled = true;
    btn.textContent = "Saving...";

    var id = document.getElementById("claimId").value;
    var params = [
        "serial_number=" + encodeURIComponent(document.getElementById("serialNumber").value),
        "serving_retired=" + encodeURIComponent(document.getElementById("servingRetired").value),
        "staff_number=" + encodeURIComponent(document.getElementById("staffNumber").value),
        "name=" + encodeURIComponent(document.getElementById("claimName").value),
        "claimed_amount=" + encodeURIComponent(document.getElementById("claimedAmount").value),
        "meeting_number=" + encodeURIComponent(document.getElementById("meetingNumber").value),
        "meeting_date=" + encodeURIComponent(document.getElementById("meetingDate").value),
        "approval_status=" + encodeURIComponent(document.getElementById("approvalStatus").value),
        "passed_amount=" + encodeURIComponent(document.getElementById("passedAmount").value),
        "final_status=" + encodeURIComponent(document.getElementById("finalStatus").value),
        "unpaid_reason=" + encodeURIComponent(document.getElementById("unpaidReason").value)
    ];
    if (id) params.push("id=" + id);

    var url = CONTEXT_PATH + "/claims?action=save";
    var xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                closeModal();
                loadClaims();
            } else {
                try {
                    var err = JSON.parse(xhr.responseText);
                    alert(err.error || "Error saving claim");
                } catch (e) {
                    alert("Error saving claim");
                }
                btn.disabled = false;
                btn.textContent = "Save";
            }
        }
    };
    xhr.send(params.join("&"));
}

// =================================================================
// EDIT CLAIM: fetch then open modal
// =================================================================
function editClaim(id) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", CONTEXT_PATH + "/claims?action=get&id=" + id, true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    var claim = JSON.parse(xhr.responseText);
                    openModal(claim);
                } catch (e) {
                    alert("Error loading claim");
                }
            } else {
                alert("Claim not found");
            }
        }
    };
    xhr.send();
}

// =================================================================
// DELETE CLAIM
// =================================================================
function deleteClaim(id) {
    if (!confirm("Delete this claim permanently?")) return;

    var xhr = new XMLHttpRequest();
    xhr.open("POST", CONTEXT_PATH + "/claims?action=delete&id=" + id, true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                loadClaims();
            } else {
                alert("Error deleting claim");
            }
        }
    };
    xhr.send();
}

// =================================================================
// CLEAR FILTERS
// =================================================================
function clearFilters() {
    document.getElementById("searchInput").value = "";
    document.getElementById("statusFilter").value = "All";
    document.getElementById("employeeFilter").value = "All";
    document.getElementById("dateFrom").value = "";
    document.getElementById("dateTo").value = "";
    loadClaims();
}

// =================================================================
// EXPORT TO EXCEL
// =================================================================
function exportExcel() {
    window.location.href = CONTEXT_PATH + "/claims?action=export";
}

// =================================================================
// CHECK BACKUP STATUS
// =================================================================
function checkBackup() {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", CONTEXT_PATH + "/claims?action=backup-status", true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            try {
                var data = JSON.parse(xhr.responseText);
                document.getElementById("rollbackBtn").disabled = !data.has_backup;
            } catch (e) {}
        }
    };
    xhr.send();
}

// =================================================================
// IMPORT FROM EXCEL
// =================================================================
function handleImport(input) {
    var file = input.files[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith(".xlsx")) {
        alert("Please select an .xlsx file.");
        input.value = "";
        return;
    }

    if (!confirm("This will REPLACE the entire current database with the contents of the uploaded file.\n\nThe current database will be saved as a backup so it can be rolled back. Continue?")) {
        input.value = "";
        return;
    }

    var fd = new FormData();
    fd.append("file", file);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", CONTEXT_PATH + "/claims?action=import", true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            try {
                var data = JSON.parse(xhr.responseText);
                if (xhr.status !== 200) {
                    var msg = "Import failed: " + (data.error || "Unknown error");
                    if (data.errors && data.errors.length) {
                        msg += "\n\nIssues:\n" + data.errors.slice(0, 10).join("\n");
                    }
                    alert(msg);
                } else {
                    var msg = "Import successful!\nInserted: " + data.inserted + "\nSkipped: " + data.skipped;
                    if (data.errors && data.errors.length) {
                        msg += "\n\nIssues (" + data.errors.length + "):\n" + data.errors.slice(0, 10).join("\n");
                        if (data.errors.length > 10) msg += "\n...and " + (data.errors.length - 10) + " more";
                    }
                    alert(msg);
                    loadClaims();
                    checkBackup();
                }
            } catch (e) {
                alert("Network error during import.");
            }
            input.value = "";
        }
    };
    xhr.send(fd);
}

// =================================================================
// ROLLBACK
// =================================================================
function rollback() {
    if (!confirm("Restore the previous database from backup?\n\nThe current database will be replaced and this backup will be consumed. Continue?")) return;

    var xhr = new XMLHttpRequest();
    xhr.open("POST", CONTEXT_PATH + "/claims?action=rollback", true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            try {
                var data = JSON.parse(xhr.responseText);
                if (xhr.status !== 200) {
                    alert("Rollback failed: " + (data.error || "Unknown error"));
                } else {
                    alert("Database restored from backup.");
                    loadClaims();
                    checkBackup();
                }
            } catch (e) {
                alert("Network error during rollback.");
            }
        }
    };
    xhr.send();
}

// =================================================================
// INITIAL LOAD
// =================================================================
toggleUnpaidReason();
checkBackup();
loadClaims();
