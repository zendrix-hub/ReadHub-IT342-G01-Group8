import React, { useState, useEffect, useCallback } from 'react'; // Import useCallback
import { LayoutGrid, History, Clock, Library, LayoutDashboard } from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import BrowseView from './BrowseView';
import ActivityView from './ActivityView';
import UrgentActionModal from '../../components/modals/UrgentActionModal'; // Import Modal
import { useAuth } from '../../context/AuthContext'; // Import Auth
import { BorrowingActivityChart } from '../../components/AnalyticsCharts';
import '../../styles/dashboard.scss';

const StudentDashboard = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [studentStats, setStudentStats] = useState(null);
  
  // --- ADVISORY LOGIC ---
  const [showAdvisory, setShowAdvisory] = useState(false);
  const [overdueItems, setOverdueItems] = useState([]);
  const [dueSoonItems, setDueSoonItems] = useState([]);
  const { token } = useAuth();

  // Helper: Days Remaining (Same as Admin)
  const getDaysRemaining = (dueDateString) => {
    if (!dueDateString) return null;
    const due = new Date(dueDateString);
    const today = new Date();
    due.setHours(0,0,0,0); today.setHours(0,0,0,0);
    return Math.ceil((due - today) / (1000 * 60 * 60 * 24)); 
  };

  const checkUrgency = useCallback(async () => {
    try {
      const res = await fetch('http://localhost:8080/api/transactions/my-history', {
        headers: { Authorization: `Bearer ${token}` }
      });
      const data = await res.json();
      const historyList = data.data || data;
      
      const activeLoans = historyList.filter(t => t.status === 'BORROWED' || t.status === 'OVERDUE');
      
      const overdue = activeLoans.filter(t => t.status === 'OVERDUE' || getDaysRemaining(t.dueDate) < 0);
      const dueSoon = activeLoans.filter(t => {
        const days = getDaysRemaining(t.dueDate);
        return days >= 0 && days <= 2; // Due within 2 days
      });
 
      const statsRes = await fetch('http://localhost:8080/api/dashboard/student', {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (statsRes.ok) {
        const statsData = await statsRes.json();
        setStudentStats(statsData.data || statsData);
      }

      if (overdue.length > 0 || dueSoon.length > 0) {
        setOverdueItems(overdue);
        setDueSoonItems(dueSoon);
        setShowAdvisory(true);
      }
    } catch(e) { console.error(e); }
  }, [token]);

  useEffect(() => {
    checkUrgency();
  }, [checkUrgency]);

  // Auto-refresh every 5 seconds
useEffect(() => {
  const interval = setInterval(() => {
    checkUrgency();  // refresh data
  }, 5000); // 5 second

  return () => clearInterval(interval); // cleanup
}, [checkUrgency]);


  const renderOverview = () => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '30px', animation: 'fadeIn 0.4s ease-out' }}>
      {/* 4 Glass Stats Cards */}
      <div className="stats-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
        <div className="stat-card glass-panel" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
            <History size={16} color="var(--color-maroon)" /> Total Requests
          </span>
          <span className="stat-value" style={{ fontSize: '32px', fontWeight: '800', color: 'var(--color-maroon)' }}>{studentStats?.totalBorrows ?? 0}</span>
        </div>
        <div className="stat-card glass-panel" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
            <Clock size={16} color="var(--color-success)" /> Active Loans
          </span>
          <span className="stat-value" style={{ fontSize: '32px', fontWeight: '800', color: 'var(--color-success)' }}>{studentStats?.activeLoans ?? 0}</span>
        </div>
        <div className="stat-card glass-panel" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
            <LayoutGrid size={16} color="var(--color-gold)" /> Pending Requests
          </span>
          <span className="stat-value" style={{ fontSize: '32px', fontWeight: '800', color: 'var(--color-gold)' }}>{studentStats?.pendingRequests ?? 0}</span>
        </div>
        <div className="stat-card glass-panel" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
            <Library size={16} color="var(--text-primary)" /> Favorite Genre
          </span>
          <span className="stat-value" style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-primary)', marginTop: '4px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{studentStats?.favoriteCategory ?? 'None'}</span>
        </div>
      </div>

      {/* SVG Trend Line/Bar Chart */}
      <div className="charts-grid" style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '30px', marginTop: '10px' }}>
        <div className="chart-card glass-panel">
          <h3 className="chart-title" style={{ fontSize: '15px', fontWeight: '700', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid var(--color-border)', paddingBottom: '12px' }}>
            <History size={16} color="var(--color-maroon)" /> My Borrowing Trend (Last 6 Months)
          </h3>
          <BorrowingActivityChart data={studentStats?.borrowingTrends} />
        </div>
      </div>
    </div>
  );

  return (
    <DashboardLayout>
      {/* ... (Existing Action Bar code) ... */}
      <div className="action-bar">
        <div className="tabs-group">
          <button 
            className={`tab-btn ${activeTab === 'overview' ? 'active' : 'inactive'}`}
            onClick={() => setActiveTab('overview')}
          >
            <LayoutDashboard size={18} />
            Overview
          </button>

          <button 
            className={`tab-btn ${activeTab === 'browse' ? 'active' : 'inactive'}`}
            onClick={() => setActiveTab('browse')}
          >
            <LayoutGrid size={18} />
            Browse Items
          </button>
          
          <button 
            className={`tab-btn ${activeTab === 'activity' ? 'active' : 'inactive'}`}
            onClick={() => setActiveTab('activity')}
          >
            <History size={18} />
            My Activity
          </button>
        </div>
      </div>

      <div style={{ width: '100%' }}>
        {activeTab === 'overview' && renderOverview()}
        {activeTab === 'browse' && <BrowseView />}
        {activeTab === 'activity' && <ActivityView />}
      </div>

      {/* --- THE ADVISORY MODAL --- */}
      {showAdvisory && (
        <UrgentActionModal 
          onClose={() => setShowAdvisory(false)}
          overdueItems={overdueItems}
          dueSoonItems={dueSoonItems}
        />
      )}

    </DashboardLayout>
  );
};

export default StudentDashboard;