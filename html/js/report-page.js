'use strict';
const API_BASE_URL = window.location.origin;
const REPORTS_API_URL = API_BASE_URL + "/api/reports";

const REPORT_DASHBOARD_URL =
    REPORTS_API_URL + "/dashboard";

const REPORT_SEARCH_URL =
    REPORTS_API_URL + "/search";

const REPORT_ANALYTICS_URL =
    REPORTS_API_URL + "/analytics";

const REPORT_ACTIVITIES_URL =
    REPORTS_API_URL + "/activities";

const DEFAULT_PAGE = 0;
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT_BY = "lastRunAt";
const DEFAULT_SORT_DIRECTION = "DESC";
const DEFAULT_ACTIVITY_LIMIT = 10;

let reports = [];
let selectedReport = null;

let currentPage = DEFAULT_PAGE;
let pageSize = DEFAULT_PAGE_SIZE;
let totalElements = 0;
let totalPages = 0;
let firstPage = true;
let lastPage = true;

let searchTimerId = null;
let searchRequestSequence = 0;
let detailRequestSequence = 0;

document.addEventListener(
    "DOMContentLoaded",
    function () {
        bindEvents();
        initializeReportsPage();
    }
);

async function initializeReportsPage() {
    resetReportPanel();
    resetDashboard();
    renderEmptyAnalytics();
    renderActivities([]);

    await Promise.allSettled([
        loadDashboard(),
        searchReports(DEFAULT_PAGE),
        loadAnalytics(),
        loadActivities()
    ]);
}

function bindEvents() {
    const reportSearchInput =
        document.getElementById("reportSearchInput");

    const globalSearchInput =
        document.getElementById("globalSearchInput");

    const categoryFilter =
        document.getElementById("categoryFilter");

    const statusFilter =
        document.getElementById("statusFilter");

    const periodFilter =
        document.getElementById("periodFilter");

    reportSearchInput.addEventListener(
        "input",
        function () {
            globalSearchInput.value =
                reportSearchInput.value;

            scheduleSearch();
        }
    );

    globalSearchInput.addEventListener(
        "input",
        function () {
            reportSearchInput.value =
                globalSearchInput.value;

            scheduleSearch();
        }
    );

    categoryFilter.addEventListener(
        "change",
        function () {
            searchReports(DEFAULT_PAGE);
        }
    );

    statusFilter.addEventListener(
        "change",
        function () {
            searchReports(DEFAULT_PAGE);
        }
    );

    periodFilter.addEventListener(
        "change",
        function () {
            searchReports(DEFAULT_PAGE);
            loadAnalytics();
        }
    );

    document
        .getElementById("openReportButton")
        .addEventListener(
            "click",
            function () {
                openSelectedReport();
            }
        );

    document
        .getElementById("exportPdfButton")
        .addEventListener(
            "click",
            function () {
                exportSelectedReport("PDF");
            }
        );

    document
        .getElementById("exportExcelButton")
        .addEventListener(
            "click",
            function () {
                exportSelectedReport("EXCEL");
            }
        );

    document
        .getElementById("scheduleButton")
        .addEventListener(
            "click",
            function () {
                openScheduleConfiguration();
            }
        );
}

function scheduleSearch() {
    window.clearTimeout(searchTimerId);

    searchTimerId = window.setTimeout(
        function () {
            searchReports(DEFAULT_PAGE);
        },
        350
    );
}

/*
 * Dashboard
 */

async function loadDashboard() {
    try {
        const responseBody = await apiRequest(
            REPORT_DASHBOARD_URL,
            {
                method: "GET"
            }
        );

        const dashboard = responseBody.data;

        if (!dashboard) {
            throw new Error(
                "Report dashboard response does not contain data."
            );
        }

        renderDashboard(dashboard);
    } catch (error) {
        console.error(
            "Unable to load report dashboard:",
            error
        );

        resetDashboard();

        showToast(
            error.message ||
            "Unable to load report dashboard."
        );
    }
}

function renderDashboard(dashboard) {
    const currency =
        dashboard.currency || "EUR";

    setText(
        "pipelineValueStat",
        formatCompactMoney(
            dashboard.pipelineValue,
            currency
        )
    );

    setText(
        "winRateStat",
        formatPercentage(dashboard.winRate)
    );

    setText(
        "openRisksStat",
        dashboard.openRiskCount ?? 0
    );

    setText(
        "contractsCount",
        dashboard.totalContractCount ?? 0
    );

    setText(
        "proposalsCount",
        dashboard.totalProposalCount ?? 0
    );

    setText(
        "pendingReviewsCount",
        dashboard.pendingReviewCount ?? 0
    );

    setText(
        "approvalBottlenecksCount",
        dashboard.approvalBottleneckCount ?? 0
    );

    setText(
        "donutValue",
        formatCompactMoney(
            dashboard.totalPortfolioValue,
            currency
        )
    );
}

function resetDashboard() {
    setText(
        "pipelineValueStat",
        formatCompactMoney(0, "EUR")
    );

    setText("winRateStat", "0%");
    setText("openRisksStat", 0);
    setText("contractsCount", 0);
    setText("proposalsCount", 0);
    setText("pendingReviewsCount", 0);
    setText("approvalBottlenecksCount", 0);

    setText(
        "donutValue",
        formatCompactMoney(0, "EUR")
    );
}

/*
 * Search and catalog
 */

function buildSearchRequest(page) {
    const searchText =
        document
            .getElementById("reportSearchInput")
            .value
            .trim();

    const category =
        document.getElementById(
            "categoryFilter"
        ).value;

    const status =
        document.getElementById(
            "statusFilter"
        ).value;

    const period =
        document.getElementById(
            "periodFilter"
        ).value;

    return {
        searchText: searchText || null,
        category:
            category === "ALL"
                ? null
                : category,
        status:
            status === "ALL"
                ? null
                : status,
        period:
            period === "ALL"
                ? null
                : period,
        page:
            Number.isInteger(page) && page >= 0
                ? page
                : DEFAULT_PAGE,
        size: DEFAULT_PAGE_SIZE,
        sortBy: DEFAULT_SORT_BY,
        sortDirection: DEFAULT_SORT_DIRECTION
    };
}

async function searchReports(page) {
    const requestSequence =
        ++searchRequestSequence;

    showLoading(true);

    try {
        const request =
            buildSearchRequest(page);

        const responseBody =
            await apiRequest(
                REPORT_SEARCH_URL,
                {
                    method: "POST",
                    body: JSON.stringify(request)
                }
            );

        if (
            requestSequence !==
            searchRequestSequence
        ) {
            return;
        }

        const pageResponse =
            responseBody.data;

        if (!pageResponse) {
            throw new Error(
                "Report search response does not contain data."
            );
        }

        reports =
            Array.isArray(pageResponse.content)
                ? pageResponse.content
                : [];

        currentPage =
            toNumber(
                firstDefined(
                    pageResponse.page,
                    pageResponse.number,
                    request.page
                ),
                request.page
            );

        pageSize =
            toNumber(
                firstDefined(
                    pageResponse.size,
                    request.size
                ),
                request.size
            );

        totalElements =
            toNumber(
                firstDefined(
                    pageResponse.totalElements,
                    reports.length
                ),
                reports.length
            );

        totalPages =
            toNumber(
                firstDefined(
                    pageResponse.totalPages,
                    reports.length > 0 ? 1 : 0
                ),
                reports.length > 0 ? 1 : 0
            );

        firstPage =
            typeof pageResponse.first ===
            "boolean"
                ? pageResponse.first
                : currentPage === 0;

        lastPage =
            typeof pageResponse.last ===
            "boolean"
                ? pageResponse.last
                : totalPages === 0 ||
                currentPage >= totalPages - 1;

        if (
            selectedReport &&
            !reports.some(function (report) {
                return (
                    String(report.id) ===
                    String(selectedReport.id)
                );
            })
        ) {
            selectedReport = null;
            resetReportPanel();
        }

        renderReports();
    } catch (error) {
        if (
            requestSequence !==
            searchRequestSequence
        ) {
            return;
        }

        console.error(
            "Unable to search reports:",
            error
        );

        reports = [];
        currentPage = DEFAULT_PAGE;
        totalElements = 0;
        totalPages = 0;
        firstPage = true;
        lastPage = true;

        renderReports();

        showToast(
            error.message ||
            "Unable to load reports."
        );
    } finally {
        if (
            requestSequence ===
            searchRequestSequence
        ) {
            showLoading(false);
        }
    }
}

async function refreshReports() {
    selectedReport = null;
    resetReportPanel();

    await Promise.allSettled([
        loadDashboard(),
        searchReports(DEFAULT_PAGE),
        loadAnalytics(),
        loadActivities()
    ]);
}

function renderReports() {
    const tableBody =
        document.getElementById(
            "reportsTableBody"
        );

    const emptyState =
        document.getElementById(
            "emptyState"
        );

    tableBody.innerHTML = "";

    if (
        !Array.isArray(reports) ||
        reports.length === 0
    ) {
        emptyState.style.display =
            "block";

        return;
    }

    emptyState.style.display = "none";

    reports.forEach(function (report) {
        const row =
            document.createElement("tr");

        row.dataset.reportId =
            report.id;

        if (
            selectedReport &&
            String(selectedReport.id) ===
            String(report.id)
        ) {
            row.classList.add("selected");
        }

        row.innerHTML = `
        <td>
          <div class="report-title">
            ${escapeHtml(report.name || "-")}
          </div>

          <div class="report-meta">
            ${escapeHtml(
            report.description || "-"
        )}
          </div>
        </td>

        <td>
          <span class="pill ${getCategoryClass(
            report.category
        )}">
            ${getCategoryIcon(
            report.category
        )}
            ${escapeHtml(
            formatEnum(report.category)
        )}
          </span>
        </td>

        <td>
          <span class="pill ${getStatusClass(
            report.status
        )}">
            ${getStatusIcon(
            report.status
        )}
            ${escapeHtml(
            formatEnum(report.status)
        )}
          </span>
        </td>

        <td>
          <div class="report-title">
            ${escapeHtml(
            formatEnum(report.period)
        )}
          </div>
        </td>

        <td>
          <div class="report-title">
            ${escapeHtml(
            report.ownerName || "-"
        )}
          </div>
        </td>

        <td>
          <div class="report-title">
            ${escapeHtml(
            formatDateTime(
                report.lastRunAt
            )
        )}
          </div>
        </td>
      `;

        row.addEventListener(
            "click",
            function () {
                fetchReportDetail(report.id);
            }
        );

        tableBody.appendChild(row);
    });
}

/*
 * Detail
 */

async function fetchReportDetail(
    reportId
) {
    if (
        reportId === null ||
        reportId === undefined
    ) {
        showToast(
            "Report identifier is not available."
        );

        return;
    }

    const requestSequence =
        ++detailRequestSequence;

    setReportPanelLoading(true);

    try {
        const responseBody =
            await apiRequest(
                REPORTS_API_URL +
                "/" +
                encodeURIComponent(reportId),
                {
                    method: "GET"
                }
            );

        if (
            requestSequence !==
            detailRequestSequence
        ) {
            return;
        }

        const report = responseBody.data;

        if (!report) {
            throw new Error(
                "Report details response does not contain data."
            );
        }

        selectedReport = report;

        renderReports();
        renderReportPanel(report);
    } catch (error) {
        if (
            requestSequence !==
            detailRequestSequence
        ) {
            return;
        }

        console.error(
            "Unable to load report details:",
            error
        );

        selectedReport = null;
        resetReportPanel();

        showToast(
            error.message ||
            "Unable to load report details."
        );
    } finally {
        if (
            requestSequence ===
            detailRequestSequence
        ) {
            setReportPanelLoading(false);
        }
    }
}

function renderReportPanel(report) {
    document.getElementById(
        "reportPlaceholder"
    ).style.display = "none";

    document.getElementById(
        "reportContent"
    ).style.display = "block";

    setText(
        "summaryTitle",
        report.name || "-"
    );

    setText(
        "summarySubtitle",
        report.description || "-"
    );

    setText(
        "detailCategory",
        formatEnum(report.category)
    );

    setText(
        "detailStatus",
        formatEnum(report.status)
    );

    setText(
        "detailPeriod",
        formatEnum(report.period)
    );

    setText(
        "detailOwner",
        report.ownerName || "-"
    );

    setText(
        "detailSources",
        Array.isArray(
            report.sourceModules
        )
            ? report.sourceModules.join(", ")
            : "-"
    );

    setText(
        "detailLastRun",
        formatDateTime(
            report.lastRunAt
        )
    );

    configureReportActions(report);
    renderMetrics(report.metrics);
    renderInsights(report.insights);
}

function configureReportActions(
    report
) {
    const openButton =
        document.getElementById(
            "openReportButton"
        );

    const pdfButton =
        document.getElementById(
            "exportPdfButton"
        );

    const excelButton =
        document.getElementById(
            "exportExcelButton"
        );

    const scheduleButton =
        document.getElementById(
            "scheduleButton"
        );

    openButton.disabled =
        report.canOpen === false;

    pdfButton.disabled =
        report.canExportPdf === false;

    excelButton.disabled =
        report.canExportExcel === false;

    scheduleButton.disabled =
        report.canSchedule === false;
}

function renderMetrics(metrics) {
    const list =
        document.getElementById(
            "metricsList"
        );

    list.innerHTML = "";

    const metricItems =
        Array.isArray(metrics)
            ? metrics
            : [];

    if (metricItems.length === 0) {
        list.appendChild(
            createInsightItem(
                "No metrics",
                "No metrics are configured for this report."
            )
        );

        return;
    }

    metricItems.forEach(
        function (metric) {
            if (
                typeof metric === "string"
            ) {
                list.appendChild(
                    createInsightItem(
                        metric,
                        "Included in this report output."
                    )
                );

                return;
            }

            const title =
                metric.title ||
                metric.code ||
                "Metric";

            const descriptionParts = [];

            if (metric.formattedValue) {
                descriptionParts.push(
                    metric.formattedValue
                );
            }

            if (metric.description) {
                descriptionParts.push(
                    metric.description
                );
            }

            list.appendChild(
                createInsightItem(
                    title,
                    descriptionParts.join(
                        " · "
                    ) ||
                    "Included in this report output."
                )
            );
        }
    );
}

function renderInsights(insights) {
    const list =
        document.getElementById(
            "insightsList"
        );

    list.innerHTML = "";

    const insightItems =
        Array.isArray(insights)
            ? insights
            : [];

    if (
        insightItems.length === 0
    ) {
        list.appendChild(
            createInsightItem(
                "No insights",
                "No generated insights are available for this report."
            )
        );

        return;
    }

    insightItems.forEach(
        function (insight) {
            if (
                typeof insight === "string"
            ) {
                list.appendChild(
                    createInsightItem(
                        "Insight",
                        insight
                    )
                );

                return;
            }

            const title =
                insight.title ||
                formatEnum(
                    insight.severity
                ) ||
                "Insight";

            const description =
                insight.description ||
                insight.text ||
                "-";

            list.appendChild(
                createInsightItem(
                    title,
                    description
                )
            );
        }
    );
}

function createInsightItem(
    title,
    description
) {
    const item =
        document.createElement("div");

    item.className =
        "insight-item";

    item.innerHTML = `
      <div>
        <div class="insight-title">
          ${escapeHtml(title)}
        </div>

        <div class="insight-desc">
          ${escapeHtml(description)}
        </div>
      </div>
    `;

    return item;
}

function resetReportPanel() {
    document.getElementById(
        "reportPlaceholder"
    ).style.display = "flex";

    document.getElementById(
        "reportContent"
    ).style.display = "none";

    document.getElementById(
        "metricsList"
    ).innerHTML = "";

    document.getElementById(
        "insightsList"
    ).innerHTML = "";

    restoreReportPlaceholder();
}

function setReportPanelLoading(
    isLoading
) {
    const placeholder =
        document.getElementById(
            "reportPlaceholder"
        );

    const content =
        document.getElementById(
            "reportContent"
        );

    if (isLoading) {
        placeholder.style.display =
            "flex";

        content.style.display = "none";

        placeholder.innerHTML = `
        <div>
          <div class="report-placeholder-icon">
            <span class="spinner"></span>
          </div>

          <h3>Loading report</h3>

          <p>
            Retrieving report metrics,
            insights and source information.
          </p>
        </div>
      `;

        return;
    }

    if (!selectedReport) {
        restoreReportPlaceholder();
    }
}

function restoreReportPlaceholder() {
    document.getElementById(
        "reportPlaceholder"
    ).innerHTML = `
      <div>
        <div class="report-placeholder-icon">
          📊
        </div>

        <h3>Select a report</h3>

        <p>
          Metrics, report description,
          generated insights, source modules,
          schedule and export actions will
          appear here.
        </p>
      </div>
    `;
}

/*
 * Analytics
 */

async function loadAnalytics() {
    try {
        const selectedPeriod =
            document.getElementById(
                "periodFilter"
            ).value;

        let url =
            REPORT_ANALYTICS_URL;

        if (selectedPeriod !== "ALL") {
            url +=
                "?period=" +
                encodeURIComponent(
                    selectedPeriod
                );
        }

        const responseBody =
            await apiRequest(
                url,
                {
                    method: "GET"
                }
            );

        const analytics =
            responseBody.data;

        if (!analytics) {
            throw new Error(
                "Report analytics response does not contain data."
            );
        }

        renderAnalytics(analytics);
    } catch (error) {
        console.error(
            "Unable to load report analytics:",
            error
        );

        renderEmptyAnalytics();

        showToast(
            error.message ||
            "Unable to load report analytics."
        );
    }
}

function renderAnalytics(
    analytics
) {
    const currency =
        analytics.currency || "EUR";

    renderPipelineBars(
        analytics.pipelineDistribution,
        currency
    );

    renderOutcomeDistribution(
        analytics.outcomeDistribution,
        analytics.totalPortfolioValue,
        currency
    );
}

function renderPipelineBars(
    pipelineDistribution,
    currency
) {
    const pipelineBars =
        document.getElementById(
            "pipelineBars"
        );

    pipelineBars.innerHTML = "";

    const items =
        Array.isArray(
            pipelineDistribution
        )
            ? pipelineDistribution
            : [];

    if (items.length === 0) {
        pipelineBars.appendChild(
            createInsightItem(
                "No pipeline data",
                "No pipeline distribution is available."
            )
        );

        return;
    }

    items.forEach(function (item) {
        const percentage =
            clampPercentage(
                item.percentage
            );

        const row =
            document.createElement("div");

        row.className = "bar-row";

        row.innerHTML = `
        <div>
          ${escapeHtml(
            item.label ||
            formatEnum(item.status)
        )}
        </div>

        <div
          class="bar-bg"
          title="${escapeHtml(
            formatPercentage(
                item.percentage
            )
        )}"
        >
          <div
            class="bar-fill"
            style="width:${percentage}%;"
          ></div>
        </div>

        <div>
          ${escapeHtml(
            formatCompactMoney(
                item.value,
                currency
            )
        )}
        </div>
      `;

        pipelineBars.appendChild(row);
    });
}

function renderOutcomeDistribution(
    outcomeDistribution,
    totalPortfolioValue,
    currency
) {
    const items =
        Array.isArray(
            outcomeDistribution
        )
            ? outcomeDistribution
            : [];

    setText(
        "donutValue",
        formatCompactMoney(
            totalPortfolioValue,
            currency
        )
    );

    const outcomeLegend =
        document.getElementById(
            "outcomeLegend"
        );

    outcomeLegend.innerHTML = "";

    if (items.length === 0) {
        outcomeLegend.appendChild(
            createInsightItem(
                "No outcome data",
                "No bid outcome distribution is available."
            )
        );

        updateDonutBackground([]);

        return;
    }

    updateDonutBackground(items);

    items.forEach(function (item) {
        const title =
            item.label ||
            formatEnum(item.outcome);

        const description =
            formatCompactMoney(
                item.value,
                currency
            ) +
            " · " +
            formatPercentage(
                item.percentage
            );

        outcomeLegend.appendChild(
            createInsightItem(
                title,
                description
            )
        );
    });
}

function updateDonutBackground(
    items
) {
    const donut =
        document.getElementById(
            "outcomeDonut"
        ) ||
        document.querySelector(
            ".donut"
        );

    if (!donut) {
        return;
    }

    if (
        !Array.isArray(items) ||
        items.length === 0
    ) {
        donut.style.background =
            "#eef2f7";

        return;
    }

    let currentDegree = 0;

    const segments = [];

    items.forEach(
        function (item, index) {
            const percentage =
                clampPercentage(
                    item.percentage
                );

            const segmentDegrees =
                percentage * 3.6;

            const startDegree =
                currentDegree;

            const endDegree =
                index === items.length - 1
                    ? 360
                    : Math.min(
                        360,
                        currentDegree +
                        segmentDegrees
                    );

            const color =
                item.color ||
                getOutcomeColor(
                    item.outcome,
                    index
                );

            segments.push(
                color +
                " " +
                startDegree +
                "deg " +
                endDegree +
                "deg"
            );

            currentDegree = endDegree;
        }
    );

    donut.style.background =
        "conic-gradient(" +
        segments.join(", ") +
        ")";
}

function renderEmptyAnalytics() {
    document.getElementById(
        "pipelineBars"
    ).innerHTML = "";

    document.getElementById(
        "outcomeLegend"
    ).innerHTML = "";

    document
        .getElementById(
            "pipelineBars"
        )
        .appendChild(
            createInsightItem(
                "No pipeline data",
                "Pipeline analytics have not been loaded."
            )
        );

    document
        .getElementById(
            "outcomeLegend"
        )
        .appendChild(
            createInsightItem(
                "No outcome data",
                "Outcome analytics have not been loaded."
            )
        );

    updateDonutBackground([]);
}

/*
 * Activities
 */

async function loadActivities() {
    try {
        const responseBody =
            await apiRequest(
                REPORT_ACTIVITIES_URL +
                "?limit=" +
                DEFAULT_ACTIVITY_LIMIT,
                {
                    method: "GET"
                }
            );

        renderActivities(
            Array.isArray(responseBody.data)
                ? responseBody.data
                : []
        );
    } catch (error) {
        console.error(
            "Unable to load report activities:",
            error
        );

        renderActivities([]);

        showToast(
            error.message ||
            "Unable to load report activities."
        );
    }
}

function renderActivities(
    activities
) {
    const activityList =
        document.getElementById(
            "activityList"
        );

    activityList.innerHTML = "";

    const items =
        Array.isArray(activities)
            ? activities
            : [];

    if (items.length === 0) {
        const empty =
            document.createElement("div");

        empty.className =
            "activity-item";

        empty.innerHTML = `
        <div>
          <div class="activity-title">
            No recent activity
          </div>

          <div class="activity-desc">
            No report generation,
            export or scheduling activity
            is available.
          </div>
        </div>

        <div class="activity-meta">
          -
        </div>
      `;

        activityList.appendChild(empty);

        return;
    }

    items.forEach(
        function (activity) {
            const item =
                document.createElement("div");

            item.className =
                "activity-item";

            const title =
                activity.reportName ||
                formatEnum(activity.action) ||
                "Report activity";

            const description =
                activity.description || "-";

            const metadata = [
                activity.performedBy,
                formatDateTime(
                    activity.performedAt
                )
            ]
                .filter(Boolean)
                .join(" · ");

            item.innerHTML = `
          <div>
            <div class="activity-title">
              ${escapeHtml(title)}
            </div>

            <div class="activity-desc">
              ${escapeHtml(description)}
            </div>
          </div>

          <div class="activity-meta">
            ${escapeHtml(
                metadata || "-"
            )}
          </div>
        `;

            activityList.appendChild(item);
        }
    );
}

/*
 * View and export
 */

async function openSelectedReport() {
    if (!selectedReport) {
        showToast(
            "Select a report first."
        );

        return;
    }

    if (
        selectedReport.canOpen === false
    ) {
        showToast(
            "You are not authorized to open this report."
        );

        return;
    }

    await fetchBinaryAndOpen(
        REPORTS_API_URL +
        "/" +
        encodeURIComponent(
            selectedReport.id
        ) +
        "/view",
        selectedReport.name ||
        "report"
    );
}

async function exportSelectedReport(
    format
) {
    if (!selectedReport) {
        showToast(
            "Select a report first."
        );

        return;
    }

    if (
        format === "PDF" &&
        selectedReport.canExportPdf ===
        false
    ) {
        showToast(
            "You are not authorized to export this report as PDF."
        );

        return;
    }

    if (
        format === "EXCEL" &&
        selectedReport.canExportExcel ===
        false
    ) {
        showToast(
            "You are not authorized to export this report as Excel."
        );

        return;
    }

    await fetchBinaryAndDownload(
        REPORTS_API_URL +
        "/" +
        encodeURIComponent(
            selectedReport.id
        ) +
        "/export?format=" +
        encodeURIComponent(format),
        buildExportFileName(
            selectedReport.name,
            format
        )
    );
}

async function fetchBinaryAndOpen(
    url,
    windowTitle
) {
    try {
        const response =
            await authenticatedFetch(
                url,
                {
                    method: "GET"
                }
            );

        if (!response.ok) {
            throw await createResponseError(
                response
            );
        }

        const blob =
            await response.blob();

        const temporaryUrl =
            window.URL.createObjectURL(
                blob
            );

        const openedWindow =
            window.open(
                temporaryUrl,
                "_blank",
                "noopener,noreferrer"
            );

        if (!openedWindow) {
            window.URL.revokeObjectURL(
                temporaryUrl
            );

            throw new Error(
                "The browser blocked the report viewer window."
            );
        }

        window.setTimeout(
            function () {
                window.URL.revokeObjectURL(
                    temporaryUrl
                );
            },
            60000
        );

        showToast(
            "Report opened successfully."
        );
    } catch (error) {
        console.error(
            "Unable to open report:",
            error
        );

        showToast(
            error.message ||
            "Unable to open report."
        );
    }
}

async function fetchBinaryAndDownload(
    url,
    fallbackFileName
) {
    try {
        const response =
            await authenticatedFetch(
                url,
                {
                    method: "GET"
                }
            );

        if (!response.ok) {
            throw await createResponseError(
                response
            );
        }

        const blob =
            await response.blob();

        const contentDisposition =
            response.headers.get(
                "content-disposition"
            );

        const fileName =
            extractFileName(
                contentDisposition
            ) ||
            fallbackFileName;

        const temporaryUrl =
            window.URL.createObjectURL(
                blob
            );

        const link =
            document.createElement("a");

        link.href = temporaryUrl;
        link.download = fileName;

        document.body.appendChild(link);
        link.click();
        link.remove();

        window.URL.revokeObjectURL(
            temporaryUrl
        );

        showToast(
            "Report exported successfully."
        );

        await loadActivities();
    } catch (error) {
        console.error(
            "Unable to export report:",
            error
        );

        showToast(
            error.message ||
            "Unable to export report."
        );
    }
}

/*
 * Schedule
 *
 * The current HTML has no scheduling modal.
 * This uses browser prompts to collect the
 * fields required by ReportScheduleRequest.
 */

async function openScheduleConfiguration() {
    if (!selectedReport) {
        showToast(
            "Select a report first."
        );

        return;
    }

    if (
        selectedReport.canSchedule ===
        false
    ) {
        showToast(
            "You are not authorized to schedule this report."
        );

        return;
    }

    const frequencyInput =
        window.prompt(
            "Frequency: DAILY, WEEKLY, MONTHLY or QUARTERLY",
            "MONTHLY"
        );

    if (frequencyInput === null) {
        return;
    }

    const frequency =
        frequencyInput
            .trim()
            .toUpperCase();

    const allowedFrequencies = [
        "DAILY",
        "WEEKLY",
        "MONTHLY",
        "QUARTERLY"
    ];

    if (
        !allowedFrequencies.includes(
            frequency
        )
    ) {
        showToast(
            "Invalid frequency."
        );

        return;
    }

    const firstRunAt =
        window.prompt(
            "First run date and time in ISO format",
            buildSuggestedFirstRunAt()
        );

    if (firstRunAt === null) {
        return;
    }

    if (
        !isValidLocalDateTime(
            firstRunAt
        )
    ) {
        showToast(
            "First run date must use an ISO local date-time format."
        );

        return;
    }

    const timezone =
        window.prompt(
            "Timezone",
            Intl.DateTimeFormat()
                .resolvedOptions()
                .timeZone ||
            "Europe/Berlin"
        );

    if (timezone === null) {
        return;
    }

    const exportFormatInput =
        window.prompt(
            "Export format: PDF or EXCEL",
            "PDF"
        );

    if (
        exportFormatInput === null
    ) {
        return;
    }

    const exportFormat =
        exportFormatInput
            .trim()
            .toUpperCase();

    if (
        exportFormat !== "PDF" &&
        exportFormat !== "EXCEL"
    ) {
        showToast(
            "Export format must be PDF or EXCEL."
        );

        return;
    }

    const recipientsInput =
        window.prompt(
            "Recipient email addresses separated by commas",
            ""
        );

    if (
        recipientsInput === null
    ) {
        return;
    }

    const recipientEmails =
        recipientsInput
            .split(",")
            .map(function (email) {
                return email.trim();
            })
            .filter(Boolean);

    if (
        recipientEmails.length === 0
    ) {
        showToast(
            "At least one recipient email is required."
        );

        return;
    }

    const request = {
        frequency: frequency,
        firstRunAt: firstRunAt.trim(),
        timezone: timezone.trim(),
        exportFormat: exportFormat,
        recipientEmails: recipientEmails,
        enabled: true
    };

    await scheduleSelectedReport(
        request
    );
}

async function scheduleSelectedReport(
    request
) {
    try {
        const responseBody =
            await apiRequest(
                REPORTS_API_URL +
                "/" +
                encodeURIComponent(
                    selectedReport.id
                ) +
                "/schedule",
                {
                    method: "POST",
                    body: JSON.stringify(request)
                }
            );

        showToast(
            responseBody.message ||
            "Report scheduled successfully."
        );

        await Promise.allSettled([
            searchReports(currentPage),
            loadActivities()
        ]);

        await fetchReportDetail(
            selectedReport.id
        );
    } catch (error) {
        console.error(
            "Unable to schedule report:",
            error
        );

        showToast(
            error.message ||
            "Unable to schedule report."
        );
    }
}

/*
 * HTTP utilities
 */

async function apiRequest(
    url,
    options
) {
    const response =
        await authenticatedFetch(
            url,
            options
        );

    let responseBody = null;

    const contentType =
        response.headers.get(
            "content-type"
        ) || "";

    if (
        contentType.includes(
            "application/json"
        )
    ) {
        responseBody =
            await response.json();
    } else {
        const responseText =
            await response.text();

        responseBody = {
            success: response.ok,
            message:
                responseText ||
                response.statusText,
            data: null
        };
    }

    if (!response.ok) {
        throw new Error(
            firstDefined(
                responseBody &&
                responseBody.message,
                "Request failed with status " +
                response.status
            )
        );
    }

    if (!responseBody) {
        throw new Error(
            "Backend returned an empty response."
        );
    }

    if (
        responseBody.success === false
    ) {
        throw new Error(
            responseBody.message ||
            "Backend request failed."
        );
    }

    return responseBody;
}

async function authenticatedFetch(
    url,
    options
) {
    const token =
        getAccessToken();

    const requestOptions = {
        method:
            options &&
            options.method
                ? options.method
                : "GET",
        headers: {
            "Accept": "*/*"
        }
    };

    if (
        options &&
        options.body !== undefined
    ) {
        requestOptions.body =
            options.body;

        requestOptions.headers[
            "Content-Type"
            ] = "application/json";
    }

    if (token) {
        requestOptions.headers[
            "Authorization"
            ] = "Bearer " + token;
    }

    return fetch(
        url,
        requestOptions
    );
}

async function createResponseError(
    response
) {
    const contentType =
        response.headers.get(
            "content-type"
        ) || "";

    if (
        contentType.includes(
            "application/json"
        )
    ) {
        const body =
            await response.json();

        return new Error(
            body.message ||
            "Request failed with status " +
            response.status
        );
    }

    const text =
        await response.text();

    return new Error(
        text ||
        "Request failed with status " +
        response.status
    );
}

function getAccessToken() {
    if (
        window.keycloak &&
        window.keycloak.token
    ) {
        return window.keycloak.token;
    }

    return (
        localStorage.getItem(
            "access_token"
        ) ||
        sessionStorage.getItem(
            "access_token"
        ) ||
        ""
    );
}

/*
 * Navigation and formatting
 */

function goToPage(url) {
    window.location.href = url;
}

function getCategoryClass(
    category
) {
    switch (category) {
        case "CONTRACT":
            return "contract";

        case "PROPOSAL":
            return "proposal";

        case "REVIEW":
            return "review";

        case "APPROVAL":
            return "approval";

        case "EXECUTIVE":
            return "executive";

        case "AI":
            return "ai";

        default:
            return "ai";
    }
}

function getCategoryIcon(
    category
) {
    switch (category) {
        case "CONTRACT":
            return "📂";

        case "PROPOSAL":
            return "📝";

        case "REVIEW":
            return "✅";

        case "APPROVAL":
            return "✔";

        case "EXECUTIVE":
            return "🏛️";

        case "AI":
            return "🤖";

        default:
            return "📊";
    }
}

function getStatusClass(status) {
    switch (status) {
        case "READY":
            return "ready";

        case "SCHEDULED":
            return "scheduled";

        case "DRAFT":
            return "draft";

        case "FAILED":
            return "failed";

        default:
            return "draft";
    }
}

function getStatusIcon(status) {
    switch (status) {
        case "READY":
            return "✅";

        case "SCHEDULED":
            return "🕘";

        case "DRAFT":
            return "📝";

        case "FAILED":
            return "❌";

        default:
            return "📊";
    }
}

function getOutcomeColor(
    outcome,
    index
) {
    const colors = [
        "#7c3aed",
        "#059669",
        "#ea580c",
        "#0284c7",
        "#dc2626",
        "#334155"
    ];

    switch (outcome) {
        case "ACTIVE":
            return "#7c3aed";

        case "WON":
            return "#059669";

        case "LOST":
            return "#ea580c";

        case "WITHDRAWN":
            return "#0284c7";

        default:
            return colors[
            index % colors.length
                ];
    }
}

function formatEnum(value) {
    if (!value) {
        return "-";
    }

    return String(value)
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(
            /\b\w/g,
            function (character) {
                return character.toUpperCase();
            }
        );
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (
        Number.isNaN(date.getTime())
    ) {
        return String(value);
    }

    return new Intl.DateTimeFormat(
        "en-GB",
        {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }
    ).format(date);
}

function formatCompactMoney(
    value,
    currency
) {
    const numericValue =
        Number(value) || 0;

    return new Intl.NumberFormat(
        "en-DE",
        {
            style: "currency",
            currency:
                currency || "EUR",
            notation: "compact",
            maximumFractionDigits: 1
        }
    ).format(numericValue);
}

function formatPercentage(value) {
    const numericValue =
        Number(value);

    if (
        !Number.isFinite(
            numericValue
        )
    ) {
        return "0%";
    }

    return new Intl.NumberFormat(
        "en-GB",
        {
            maximumFractionDigits: 2
        }
    ).format(numericValue) + "%";
}

function clampPercentage(value) {
    const numericValue =
        Number(value);

    if (
        !Number.isFinite(
            numericValue
        )
    ) {
        return 0;
    }

    return Math.min(
        100,
        Math.max(0, numericValue)
    );
}

function buildExportFileName(
    reportName,
    format
) {
    const safeName =
        String(
            reportName || "report"
        )
            .trim()
            .replace(
                /[^a-zA-Z0-9-_]+/g,
                "-"
            )
            .replace(
                /^-+|-+$/g,
                ""
            ) || "report";

    const extension =
        format === "EXCEL"
            ? ".xlsx"
            : ".pdf";

    return safeName + extension;
}

function extractFileName(
    contentDisposition
) {
    if (!contentDisposition) {
        return null;
    }

    const utf8Match =
        contentDisposition.match(
            /filename\*=UTF-8''([^;]+)/i
        );

    if (
        utf8Match &&
        utf8Match[1]
    ) {
        return decodeURIComponent(
            utf8Match[1]
        );
    }

    const normalMatch =
        contentDisposition.match(
            /filename="?([^";]+)"?/i
        );

    return normalMatch &&
    normalMatch[1]
        ? normalMatch[1]
        : null;
}

function buildSuggestedFirstRunAt() {
    const date = new Date();

    date.setDate(
        date.getDate() + 1
    );

    date.setHours(
        8,
        0,
        0,
        0
    );

    const year =
        date.getFullYear();

    const month =
        String(
            date.getMonth() + 1
        ).padStart(2, "0");

    const day =
        String(date.getDate())
            .padStart(2, "0");

    return (
        year +
        "-" +
        month +
        "-" +
        day +
        "T08:00:00"
    );
}

function isValidLocalDateTime(
    value
) {
    if (!value) {
        return false;
    }

    const localDateTimePattern =
        /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?$/;

    if (
        !localDateTimePattern.test(
            value.trim()
        )
    ) {
        return false;
    }

    const date =
        new Date(value);

    return !Number.isNaN(
        date.getTime()
    );
}

function escapeHtml(value) {
    if (
        value === null ||
        value === undefined
    ) {
        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function firstDefined() {
    for (
        let index = 0;
        index <
        arguments.length;
        index++
    ) {
        const value =
            arguments[index];

        if (
            value !== undefined &&
            value !== null
        ) {
            return value;
        }
    }

    return null;
}

function toNumber(
    value,
    fallback
) {
    const numericValue =
        Number(value);

    return Number.isFinite(
        numericValue
    )
        ? numericValue
        : fallback;
}

function setText(
    elementId,
    value
) {
    const element =
        document.getElementById(
            elementId
        );

    if (!element) {
        return;
    }

    element.textContent =
        value === null ||
        value === undefined
            ? "-"
            : String(value);
}

function showLoading(
    isLoading
) {
    document.getElementById(
        "loadingIndicator"
    ).style.display =
        isLoading
            ? "flex"
            : "none";
}

function showToast(message) {
    const toast =
        document.getElementById(
            "toast"
        );

    toast.textContent =
        message ||
        "Operation completed.";

    toast.style.display =
        "block";

    window.clearTimeout(
        showToast.timeoutId
    );

    showToast.timeoutId =
        window.setTimeout(
            function () {
                toast.style.display =
                    "none";
            },
            3000
        );
}