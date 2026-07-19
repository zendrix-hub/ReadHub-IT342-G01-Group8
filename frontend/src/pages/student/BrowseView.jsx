import React, { useEffect, useState, useCallback } from 'react';
import { Search, X, Calculator, Book, Box, Zap } from 'lucide-react';
import BookCard from '../../components/cards/BookCard';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext'; 
import { useConfirm } from '../../context/ConfirmationContext'; 

// --- HELPER: Status Colors ---
const getStatusColor = (status) => {
  switch (status) {
    case 'PENDING': return '#D97706'; // Amber/Orange
    case 'APPROVED': return '#059669'; // Green
    case 'BORROWED': return '#2563EB'; // Blue
    case 'OVERDUE': return '#DC2626';  // Red
    case 'RETURNED': return '#4B5563'; // Gray
    default: return '#111827';
  }
};

// --- MODAL COMPONENT (Internal) ---
const BookDetailModal = ({ book, onClose, onRequest, isProcessing }) => {
  if (!book) return null;

  // Visual Helpers
  const getItemIcon = (title) => {
    const t = title.toLowerCase();
    if (t.includes('calculator')) return <Calculator size={24} />;
    if (t.includes('multimeter') || t.includes('arduino')) return <Zap size={24} />;
    if (t.includes('kit')) return <Box size={24} />;
    return <Book size={24} />;
  };

  const getIconStyle = (title) => {
    const t = title.toLowerCase();
    if (t.includes('calculator')) return '#FEF9C3';
    if (t.includes('multimeter') || t.includes('arduino')) return '#FFF7ED';
    return '#FEF9C3';
  };

  const isAvailable = book.availableCopies > 0;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()} style={{ width: '600px' }}>
        
        <button onClick={onClose} className="btn-close" style={{ position: 'absolute', top: '24px', right: '24px' }}>
          <X size={24} />
        </button>

        <div style={{ padding: '32px' }}>
          <div style={{ display: 'flex', gap: '20px', marginBottom: '24px' }}>
            <div style={{ width: '64px', height: '64px', borderRadius: '12px', background: getIconStyle(book.title), display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#B45309' }}>
              {getItemIcon(book.title)}
            </div>
            <div>
              <h2 style={{ fontSize: '20px', fontWeight: '700', color: '#111827', margin: '0 0 4px 0' }}>{book.title}</h2>
              <p style={{ color: '#6B7280', margin: 0 }}>by {book.author}</p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
            <span className={`badge ${isAvailable ? 'badge-available' : 'badge-checked-out'}`}>
              {isAvailable ? 'Available' : 'Checked Out'}
            </span>
            <span className="badge badge-outline-yellow">Excellent Condition</span>
          </div>

          <p style={{ fontSize: '14px', color: '#6B7280', lineHeight: '1.6', marginBottom: '24px' }}>
            Advanced resource suitable for higher education students. This item includes all standard features expected for the curriculum.
          </p>

          <div className="modal-footer" style={{ padding: 0, border: 'none', background: 'transparent' }}>
            <button onClick={onClose} className="btn-cancel" style={{ flex: 1 }}>Close</button>
            <button 
              onClick={() => onRequest(book.bookId)}
              disabled={!isAvailable || isProcessing}
              className={`btn-confirm ${isProcessing ? 'btn-animate-pop' : ''}`}
              style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px' }}
            >
              {isProcessing ? <span className="spinner-sm"></span> : 'Request Book'}
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};

// --- MAIN COMPONENT ---
const BrowseView = () => {
  const [books, setBooks] = useState([]);
  const [history, setHistory] = useState([]); 
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedKeyword, setDebouncedKeyword] = useState('');
  const [selectedBook, setSelectedBook] = useState(null);
  const [processingId, setProcessingId] = useState(null); 

  const { token } = useAuth();
  const { showToast } = useToast();
  const { confirm } = useConfirm();

  // Debounce keyword state changes by 300ms
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedKeyword(searchTerm);
    }, 300);
    return () => clearTimeout(handler);
  }, [searchTerm]);

  // 1. Fetch Books AND History
  const loadData = useCallback(() => {
    // Fetch Books
    fetch(`http://localhost:8080/api/books?keyword=${debouncedKeyword}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => setBooks(data))
    .catch(err => console.error(err));

    // Fetch History (to check for duplicates)
    fetch('http://localhost:8080/api/transactions/my-history', {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => setHistory(data))
    .catch(err => console.error(err));
  }, [token, debouncedKeyword]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
  if (!token) return;

  const interval = setInterval(() => {
    console.log("Refreshing BrowseView...");
    loadData();
  }, 5000); // refresh every 5 seconds

  return () => clearInterval(interval);
}, [token, loadData]); // <- depends on token and loadData


  const handleBorrow = async (bookId) => {
    // 1. Check for Duplicates in History
    const activeTxn = history.find(t => 
      t.book.bookId === bookId && 
      (t.status === 'PENDING' || t.status === 'APPROVED' || t.status === 'BORROWED' || t.status === 'OVERDUE')
    );

    if (activeTxn) {
      // JSX Content for the Confirm Modal
      const warningContent = (
        <div style={{ textAlign: 'center' }}>
          <span>You already have an active request or loan for this item.</span>
          <br />
          <div style={{ 
            marginTop: '16px', 
            marginBottom: '16px', 
            padding: '12px', 
            background: '#F3F4F6', 
            borderRadius: '8px',
            border: '1px solid #E5E7EB',
            fontWeight: '600'
          }}>
            Current Status: <span style={{ color: getStatusColor(activeTxn.status), fontSize: '15px', marginLeft: '4px' }}>{activeTxn.status}</span>
          </div>
          <span style={{ fontSize: '13px', color: '#6B7280' }}>Are you sure you want to request another copy?</span>
        </div>
      );

      const isConfirmed = await confirm(warningContent, "Duplicate Request Warning");
      if (!isConfirmed) return;
    }

    // 2. Proceed with Request
    setProcessingId(bookId); 
    
    try {
      const res = await fetch('http://localhost:8080/api/transactions/borrow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ bookId })
      });

      if (res.ok) {
        showToast('Request Sent Successfully!', 'success');
        setSelectedBook(null);
        loadData(); // Refresh books and history immediately
      } else {
        const errData = await res.json();
        showToast(errData.message || 'Failed to request book.', 'error');
      }
    } catch (e) {
      showToast('Network error', 'error');
    } finally {
      setProcessingId(null); 
    }
  };

  return (
    <div style={{ width: '100%' }}>
      <div className="search-filter-container" style={{ display: 'flex', gap: '16px', marginBottom: '32px' }}>
        <div className="search-input-wrapper" style={{ flex: 1, position: 'relative' }}>
          <Search className="search-icon" size={20} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: '#9CA3AF' }} />
          <input 
            type="text" 
            className="search-input" 
            placeholder="Search items by title, author, or ISBN..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="books-grid">
        {books.map(book => (
          <BookCard 
            key={book.bookId} 
            book={book} 
            onAction={handleBorrow} 
            onView={(book) => setSelectedBook(book)} 
          />
        ))}
      </div>

      {selectedBook && (
        <BookDetailModal 
          book={selectedBook} 
          onClose={() => setSelectedBook(null)} 
          onRequest={handleBorrow} 
          isProcessing={processingId === selectedBook.bookId}
        />
      )}
    </div>
  );
};

export default BrowseView;