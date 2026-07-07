// customer-value-matrix.js
// Renders a packed bubble (Circle Packing) chart using D3.js based on the Conversion Rate Value Matrix API

document.addEventListener('DOMContentLoaded', () => {
    const svgElement = document.getElementById('cvmChart');
    const tooltip = document.getElementById('cvmTooltip');
    if (!svgElement) {
        console.error('SVG element not found');
        return;
    }

    // Helper format currency (in Million/Billion VND or standard format)
    const formatCurrency = (val) => {
        if (val === undefined || val === null) return '0 triệu';
        const valInMil = val / 1e6;
        const formatted = Number(valInMil.toFixed(2)).toLocaleString('vi-VN');
        return (val < 0 ? '-' : '') + formatted + ' triệu';
    };

    fetch('/api/dashboard/conversionrate-value-matrix')
        .then(res => {
            if (!res.ok) throw new Error('Network response not ok');
            return res.json();
        })
        .then(data => {
            const svg = d3.select(svgElement);
            const width = 850;
            const height = 550;

            // Clear any existing contents
            svgElement.innerHTML = '';

            // 1. Group Classification based on medians
            const winRates = data.map(d => d.conversionRate || 0);
            const avgRevs = data.map(d => d.avgRevenuePerWon || 0);
            
            const getMedian = (arr) => {
                if (arr.length === 0) return 0;
                const sorted = [...arr].sort((a, b) => a - b);
                const mid = Math.floor(sorted.length / 2);
                return sorted.length % 2 !== 0 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
            };
            
            const winRateThreshold = getMedian(winRates) || 15;
            const revenueThreshold = getMedian(avgRevs) || 100000000;

            const groupsConfig = {
                easy_high: {
                    name: 'Dễ thắng + Giá trị cao',
                    color: '#10b981', // Emerald
                    bgOpacity: 0.03
                },
                hard_high: {
                    name: 'Khó thắng + Giá trị cao',
                    color: '#3b82f6', // Blue
                    bgOpacity: 0.03
                },
                easy_low: {
                    name: 'Dễ thắng + Giá trị thấp',
                    color: '#f59e0b', // Amber
                    bgOpacity: 0.03
                },
                hard_low: {
                    name: 'Khó thắng + Giá trị thấp',
                    color: '#ef4444', // Rose
                    bgOpacity: 0.03
                }
            };

            // Prepare hierarchy structure
            const rootData = {
                name: "Customer Value Matrix",
                children: [
                    { name: groupsConfig.easy_high.name, key: "easy_high", children: [] },
                    { name: groupsConfig.hard_high.name, key: "hard_high", children: [] },
                    { name: groupsConfig.easy_low.name, key: "easy_low", children: [] },
                    { name: groupsConfig.hard_low.name, key: "hard_low", children: [] }
                ]
            };

            // Compress high dynamic range of revenues using square root mapping
            const maxRev = d3.max(data, d => d.revenueWon || 0) || 1;
            const scaleValue = (val) => {
                return 15 + (Math.sqrt(Math.max(val, 0)) / Math.sqrt(maxRev)) * 85;
            };

            // Classify segments into quadrants
            data.forEach(item => {
                const isEasy = (item.conversionRate || 0) >= winRateThreshold;
                const isHigh = (item.avgRevenuePerWon || 0) >= revenueThreshold;
                
                let key = "hard_low";
                if (isEasy && isHigh) key = "easy_high";
                else if (!isEasy && isHigh) key = "hard_high";
                else if (isEasy && !isHigh) key = "easy_low";
                
                const groupNode = rootData.children.find(c => c.key === key);
                if (groupNode) {
                    groupNode.children.push({
                        ...item,
                        name: item.segmentName,
                        value: scaleValue(item.revenueWon || 0)
                    });
                }
            });

            // Filter out empty groups
            rootData.children = rootData.children.filter(g => g.children.length > 0);

            // Build D3 Hierarchy
            const root = d3.hierarchy(rootData)
                .sum(d => d.value)
                .sort((a, b) => b.value - a.value);

            // Run Circle Packing Layout
            d3.pack()
                .size([width, height])
                .padding(18)(root);

            const g = svg.append("g");

            // Draw parent group circles and labels
            const node = g.selectAll(".node")
                .data(root.descendants().slice(1)) // Skip root
                .enter().append("g")
                .attr("class", d => d.children ? "node node--parent" : "node node--leaf")
                .attr("transform", d => `translate(${d.x},${d.y})`);

            // Group circles
            node.filter(d => d.depth === 1)
                .append("circle")
                .attr("r", d => d.r)
                .attr("fill", d => groupsConfig[d.data.key]?.color || "#cbd5e1")
                .attr("fill-opacity", d => groupsConfig[d.data.key]?.bgOpacity || 0.02)
                .attr("stroke", d => groupsConfig[d.data.key]?.color || "#94a3b8")
                .attr("stroke-width", 1.5)
                .attr("stroke-dasharray", "4,4")
                .style("pointer-events", "none");

            // Group titles
            node.filter(d => d.depth === 1)
                .append("text")
                .attr("y", d => -d.r + 14)
                .attr("text-anchor", "middle")
                .attr("fill", d => groupsConfig[d.data.key]?.color || "#475569")
                .attr("font-size", "10px")
                .attr("font-weight", "800")
                .attr("letter-spacing", "0.05em")
                .text(d => d.data.name);

            // Segment circles
            const leafGroup = node.filter(d => !d.children);

            leafGroup.append("circle")
                .attr("r", d => d.r)
                .attr("fill", d => {
                    const parentKey = d.parent.data.key;
                    return groupsConfig[parentKey]?.color || "#cbd5e1";
                })
                .attr("fill-opacity", (d, i) => {
                    return 0.70 + (i % 4) * 0.07;
                })
                .attr("stroke", d => {
                    const parentKey = d.parent.data.key;
                    return groupsConfig[parentKey]?.color || "#94a3b8";
                })
                .attr("stroke-width", 0.5)
                .style("cursor", "pointer")
                .on("mouseover", function(event, d) {
                    d3.select(this)
                        .transition()
                        .duration(150)
                        .attr("stroke-width", 2.5)
                        .attr("stroke", "#ffffff")
                        .attr("fill-opacity", 0.95);
                        
                    const tooltipEl = d3.select(tooltip);
                    tooltipEl.style("display", "block")
                        .html(`
                            <div style="font-weight: 800; border-bottom: 1px solid #f1f5f9; padding-bottom: 6px; margin-bottom: 6px; color: #1e293b;">${d.data.segmentName}</div>
                            <div style="display: flex; flex-direction: column; gap: 4px;">
                                <div style="display: flex; justify-content: space-between; gap: 20px;"><span>Nhóm:</span><span style="font-weight: 700; color: #475569;">${d.parent.data.name}</span></div>
                                <div style="display: flex; justify-content: space-between; gap: 20px;"><span>Tổng số Leads:</span><span style="font-weight: 700;">${d.data.totalLeads}</span></div>
                                <div style="display: flex; justify-content: space-between; gap: 20px;"><span>Đã thắng:</span><span style="font-weight: 700; color: #10b981;">${d.data.wonLeads}</span></div>
                                <div style="display: flex; justify-content: space-between; gap: 20px;"><span>Đã mất:</span><span style="font-weight: 700; color: #ef4444;">${d.data.lostLeads}</span></div>
                                <div style="display: flex; justify-content: space-between; gap: 20px;"><span>Tỷ lệ thắng:</span><span style="font-weight: 700; color: #3b82f6;">${d.data.conversionRate}%</span></div>
                                <div style="display: flex; justify-content: space-between; gap: 20px;"><span>Doanh thu Won:</span><span style="font-weight: 700; color: #0f172a;">${formatCurrency(d.data.revenueWon)}</span></div>
                                <div style="display: flex; justify-content: space-between; gap: 20px;"><span>Doanh thu TB/Won:</span><span style="font-weight: 700; color: #0f172a;">${formatCurrency(d.data.avgRevenuePerWon)}</span></div>
                            </div>
                        `);
                })
                .on("mousemove", function(event) {
                    const containerRect = svgElement.parentElement.getBoundingClientRect();
                    const x = event.clientX - containerRect.left;
                    const y = event.clientY - containerRect.top;
                    
                    const tooltipWidth = tooltip.offsetWidth;
                    const tooltipHeight = tooltip.offsetHeight;
                    
                    let leftPos = x + 15;
                    let topPos = y - tooltipHeight - 15;
                    
                    if (leftPos + tooltipWidth > containerRect.width) {
                        leftPos = x - tooltipWidth - 15;
                    }
                    if (topPos < 0) {
                        topPos = y + 15;
                    }
                    
                    d3.select(tooltip)
                        .style("left", leftPos + "px")
                        .style("top", topPos + "px");
                })
                .on("mouseout", function() {
                    const parentKey = d3.select(this.parentNode).datum().parent.data.key;
                    const baseColor = groupsConfig[parentKey]?.color || "#cbd5e1";
                    
                    d3.select(this)
                        .transition()
                        .duration(150)
                        .attr("stroke-width", 0.5)
                        .attr("stroke", baseColor)
                        .attr("fill-opacity", (d, i) => 0.70 + (i % 4) * 0.07);
                        
                    d3.select(tooltip).style("display", "none");
                });

            // Short labels inside segment bubbles
            leafGroup.append("text")
                .attr("text-anchor", "middle")
                .attr("fill", "#ffffff")
                .attr("font-size", d => Math.min(10, d.r / 3.2) + "px")
                .attr("font-weight", "bold")
                .style("pointer-events", "none")
                .each(function(d) {
                    const r = d.r;
                    if (r < 24) return;
                    
                    const fullName = d.data.segmentName || "";
                    const parts = fullName.split(" - ");
                    const primary = parts[0] || "";
                    
                    const el = d3.select(this);
                    if (r > 40) {
                        const words = primary.split(" & ");
                        if (words.length > 1) {
                            el.append("tspan").attr("x", 0).attr("y", -3).text(words[0] + " &");
                            el.append("tspan").attr("x", 0).attr("y", Math.min(10, r / 3.2) + 1).text(words[1]);
                        } else {
                            el.text(primary.length > 14 ? primary.substring(0, 12) + ".." : primary);
                        }
                    } else {
                        el.text(primary.length > 10 ? primary.substring(0, 8) + ".." : primary);
                    }
                });
        })
        .catch(err => {
            console.error('Failed to load matrix data', err);
            const container = document.querySelector('.chart-container');
            if (container) container.innerHTML = '<p style="color:red; text-align:center; font-family:sans-serif;">Unable to load chart data.</p>';
        });
});
