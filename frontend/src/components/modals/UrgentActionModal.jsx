import React from 'react';
import { AlertTriangle, Calendar, X } from 'lucide-react';
import '../../styles/dashboard.scss'; // Ensure we have access to styles

const UrgentActionModal = ({ onClose, overdueItems, dueSoonItems }) => {
  if (overdueItems.length === 0 && dueSoonItems.length === 0) return null;

  // Helper to generate Google Calendar Link
  const addToCalendar = (bookTitle, dueDate) => {
    const start = new Date(dueDate).toISOString().replace(/-|:|\.\d\d\d/g, "");
    const end = new Date(new Date(dueDate).getTime() + 60 * 60 * 1000).toISOString().replace(/-|:|\.\d\d\d/g, ""); // 1 hour duration
    
    const url = `https://www.google.com/calendar/render?action=TEMPLATE&text=Return+Book:+${encodeURIComponent(bookTitle)}&dates=${start}/${end}&details=Please+return+this+book+to+ReadHub+library.`;
    window.open(url, '_blank');
  };

  return (
    <div className="modal-overlay" style={{ zIndex: 9999 }}>
      <div className="modal-container" style={{ width: '500px', borderTop: '6px solid #DC2626' }}>
        
        <div className="modal-header">
          <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <div style={{ background: '#FEE2E2', padding: '12px', borderRadius: '50%', color: '#DC2626' }}>
              <AlertTriangle size={24} />
            </div>
            <div>
              <h2 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-main)', margin: 0 }}>Action Required</h2>
              <p style={{ margin: 0, color: 'var(--text-sub)', fontSize: '14px' }}>You have items that need attention.</p>
            </div>
          </div>
          <button onClick={onClose} className="btn-close"><X size={20} /></button>
        </div>

        <div className="modal-body" style={{ maxHeight: '400px', overflowY: 'auto' }}>
          
          {/* OVERDUE SECTION */}
          {overdueItems.length > 0 && (
            <div style={{ marginBottom: '24px' }}>
              <h4 style={{ color: '#DC2626', fontSize: '13px', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '12px' }}>
                ⚠️ Overdue Items (Return Immediately)
              </h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {overdueItems.map(t => (
                  <div key={t.transactionId} style={{ background: '#FEF2F2', border: '1px solid #FECACA', padding: '12px', borderRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: '600', color: '#991B1B' }}>{t.bookTitle}</div>
                      <div style={{ fontSize: '12px', color: '#B91C1C' }}>Due: {t.dueDate}</div>
                    </div>
                    <span style={{ fontSize: '20px' }}>🚨</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* DUE SOON SECTION */}
          {dueSoonItems.length > 0 && (
            <div>
              <h4 style={{ color: '#D97706', fontSize: '13px', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '12px' }}>
                ⏰ Due Soon (Don't Forget)
              </h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {dueSoonItems.map(t => (
                  <div key={t.transactionId} style={{ background: '#FFFBEB', border: '1px solid #FDE68A', padding: '12px', borderRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: '600', color: '#92400E' }}>{t.bookTitle}</div>
                      <div style={{ fontSize: '12px', color: '#B45309' }}>Due: {t.dueDate}</div>
                    </div>
                    <button 
                      onClick={() => addToCalendar(t.bookTitle, t.dueDate)}
                      title="Add to Google Calendar"
                      style={{ background: 'white', border: '1px solid #FCD34D', borderRadius: '6px', padding: '6px', cursor: 'pointer', color: '#D97706' }}
                    >
                      <Calendar size={16} />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="modal-footer" style={{ background: 'var(--bg-page)', borderTop: '1px solid var(--border-light)' }}>
          <button onClick={onClose} className="btn-confirm" style={{ width: '100%' }}>
            I Understand
          </button>
        </div>

      </div>
    </div>
  );
};

export default UrgentActionModal;