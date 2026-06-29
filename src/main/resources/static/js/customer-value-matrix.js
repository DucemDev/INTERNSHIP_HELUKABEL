// customer-value-matrix.js
// Renders a packed bubble chart using Chart.js based on the Customer Value Matrix API

document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('cvmChart');
    if (!canvas) {
        console.error('Canvas element not found');
        return;
    }
    const ctx = canvas.getContext('2d');

    const colors = [
        'rgba(255, 99, 132, 0.6)',
        'rgba(54, 162, 235, 0.6)',
        'rgba(255, 206, 86, 0.6)',
        'rgba(75, 192, 192, 0.6)',
        'rgba(153, 102, 255, 0.6)',
        'rgba(255, 159, 64, 0.6)'
    ];

    fetch('/api/dashboard/customer-value-matrix')
        .then(res => {
            if (!res.ok) throw new Error('Network response not ok');
            return res.json();
        })
        .then(data => {
            const groups = {};
            // Determine the maximum revenue to normalize bubble sizes (max radius 30)
            const maxRevenue = Math.max(...data.map(i => i.revenueWon || 0), 0);
            data.forEach(item => {
                const grp = item.customerGroup || 'Other';
                if (!groups[grp]) groups[grp] = [];
                // Normalized radius: proportional to sqrt(revenue) with a cap of ~30px
                const radius = maxRevenue ? Math.sqrt(item.revenueWon || 0) / Math.sqrt(maxRevenue) * 30 : 5;
                groups[grp].push({
                    x: item.winRate,
                    y: item.avgRevenuePerWonLead,
                    r: radius,
                    segmentName: item.segmentName,
                    revenueWon: item.revenueWon
                });
            });

            const datasets = Object.entries(groups).map(([group, pts], i) => ({
                label: group,
                data: pts,
                backgroundColor: colors[i % colors.length],
                borderColor: 'rgba(0,0,0,0.2)',
                borderWidth: 1
            }));

            new Chart(ctx, {
                type: 'bubble',
                data: { datasets },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'right' },
                        tooltip: {
                            callbacks: {
                                title: ctx => `Segment: ${ctx[0].raw.segmentName}`,
                                label: ctx => [
                                    `Win Rate: ${ctx.raw.x}%`,
                                    `Avg Rev/Lead: $${ctx.raw.y.toFixed(2)}`,
                                    `Revenue Won: $${ctx.raw.revenueWon.toLocaleString()}`
                                ].join('\n')
                            }
                        }
                    },
                    scales: {
                        x: { title: { display: true, text: 'Win Rate (%)' }, beginAtZero: true },
                        y: { title: { display: true, text: 'Avg Revenue per Won Lead ($)' }, beginAtZero: true }
                    },
                    animation: { duration: 800, easing: 'easeOutQuart' }
                }
            });
        })
        .catch(err => {
            console.error('Failed to load matrix data', err);
            const container = document.querySelector('.chart-container');
            if (container) container.innerHTML = '<p style="color:red; text-align:center;">Unable to load chart data.</p>';
        });
});
