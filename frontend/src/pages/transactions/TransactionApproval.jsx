import React, { useState, useEffect } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import StatusBadge from '../../components/status/StatusBadge';
import { useAuth } from '../../context/AuthContext';

const TransactionApproval = () => {
  const [transactions, setTransactions] = useState([]);
  const { token } = useAuth();

  const fetchTransactions = () => {
    fetch('http://localhost:8080/api/transactions', {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => setTransactions(data.data || data));
  };

  useEffect(() => { fetchTransactions(); }, [token]);

  const updateStatus = async (id, status) => {
    const res = await fetch(`http://localhost:8080/api/transactions/${id}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify({ status })
    });
    if (res.ok) fetchTransactions();
  };

  return (
    <DashboardLayout title="Manage Transactions">
      <div style={{ background: 'white', borderRadius: '8px', overflow: 'hidden', boxShadow: 'var(--shadow-subtle)' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead style={{ background: '#FAFAFA', borderBottom: '1px solid #E0E0E0' }}>
            <tr>
              <th style={{ padding: '16px' }}>Book</th>
              <th style={{ padding: '16px' }}>Student</th>
              <th style={{ padding: '16px' }}>Status</th>
              <th style={{ padding: '16px' }}>Date</th>
              <th style={{ padding: '16px', textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map(txn => (
              <tr key={txn.transactionId} style={{ borderBottom: '1px solid #F0F0F0' }}>
                <td style={{ padding: '16px', fontWeight: 600 }}>{txn.book?.title}</td>
                <td style={{ padding: '16px' }}>
                  <div>{txn.user?.firstName} {txn.user?.lastName}</div>
                  <div style={{ fontSize: '12px', color: '#8C8C8C' }}>{txn.user?.email}</div>
                </td>
                <td style={{ padding: '16px' }}><StatusBadge status={txn.status} /></td>
                <td style={{ padding: '16px' }}>{new Date(txn.requestDate).toLocaleDateString()}</td>
                <td style={{ padding: '16px', textAlign: 'right' }}>
                  <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                    {txn.status === 'PENDING' && (
                      <>
                        <button onClick={() => updateStatus(txn.transactionId, 'APPROVED')} style={{ color: '#007BFF', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}>Approve</button>
                        <button onClick={() => updateStatus(txn.transactionId, 'REJECTED')} style={{ color: '#DC3545', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}>Reject</button>
                      </>
                    )}
                    {txn.status === 'APPROVED' && (
                      <button onClick={() => updateStatus(txn.transactionId, 'BORROWED')} style={{ color: '#5CB85C', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}>Confirm Pickup</button>
                    )}
                    {txn.status === 'BORROWED' && (
                      <button onClick={() => updateStatus(txn.transactionId, 'RETURNED')} style={{ color: '#4D4D4D', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}>Mark Returned</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </DashboardLayout>
  );
};

export default TransactionApproval;