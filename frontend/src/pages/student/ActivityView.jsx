import React, { useEffect, useState } from 'react';
import { Clock } from 'lucide-react';
import StatusBadge from '../../components/status/StatusBadge';
import { useAuth } from '../../context/AuthContext';

const ActivityView = () => {
  const [history, setHistory] = useState([]);
  const { token } = useAuth();

  useEffect(() => {
    fetch('http://localhost:8080/api/transactions/my-history', {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => setHistory(data.data || data))
    .catch(err => console.error("Error fetching history:", err));
  }, [token]);

  const stats = {
    total: history.length,
    pending: history.filter(t => t.status === 'PENDING').length,
    approved: history.filter(t => t.status === 'APPROVED').length,
    returned: history.filter(t => t.status === 'RETURNED').length
  };

  return (
    <div style={{ width: '100%' }}>
      {/* STATS GRID */}
      <div className="stats-grid" style={{ marginBottom: '32px', display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '24px' }}>
        <div className="stat-card">
          <span className="stat-label">Total Requests</span>
          <span className="stat-value total">{stats.total}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Pending</span>
          <span className="stat-value pending">{stats.pending}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Approved</span>
          <span className="stat-value approved">{stats.approved}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Returned</span>
          <span className="stat-value returned">{stats.returned}</span>
        </div>
      </div>

      {/* HISTORY TABLE */}
      <div className="table-wrapper" style={{ width: '100%', background: 'var(--white)', borderRadius: '12px', boxShadow: 'var(--card-shadow)', overflow: 'hidden' }}>
        {history.length > 0 ? (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead style={{ background: 'var(--bg-page)', borderBottom: '1px solid var(--border-light)' }}>
              <tr>
                <th style={{ padding: '20px 24px', fontSize: '13px', fontWeight: '600', color: 'var(--text-sub)', textTransform: 'uppercase' }}>Book Title</th>
                <th style={{ padding: '20px 24px', fontSize: '13px', fontWeight: '600', color: 'var(--text-sub)', textTransform: 'uppercase' }}>Author</th>
                <th style={{ padding: '20px 24px', fontSize: '13px', fontWeight: '600', color: 'var(--text-sub)', textTransform: 'uppercase' }}>Status</th>
                <th style={{ padding: '20px 24px', fontSize: '13px', fontWeight: '600', color: 'var(--text-sub)', textTransform: 'uppercase' }}>Request Date</th>
                <th style={{ padding: '20px 24px', fontSize: '13px', fontWeight: '600', color: 'var(--text-sub)', textTransform: 'uppercase' }}>Due Date</th>
              </tr>
            </thead>
            <tbody>
              {history.map(txn => (
                <tr key={txn.transactionId} style={{ borderBottom: '1px solid var(--border-light)' }}>
                  <td style={{ padding: '20px 24px', fontWeight: '600', color: 'var(--text-main)' }}>{txn.bookTitle}</td>
                  <td style={{ padding: '20px 24px', color: 'var(--text-sub)' }}>{txn.bookAuthor}</td>
                  <td style={{ padding: '20px 24px' }}><StatusBadge status={txn.status} /></td>
                  <td style={{ padding: '20px 24px', color: 'var(--text-sub)' }}>{new Date(txn.requestDate).toLocaleDateString()}</td>
                  <td style={{ padding: '20px 24px', color: 'var(--text-sub)' }}>{txn.dueDate ? new Date(txn.dueDate).toLocaleDateString() : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="empty-state-card" style={{ border: 'none', boxShadow: 'none' }}>
            <Clock size={64} className="empty-icon" strokeWidth={1} />
            <h4 className="empty-title">No activity yet.</h4>
            <p className="empty-subtitle">Start browsing items to make your first request!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ActivityView;