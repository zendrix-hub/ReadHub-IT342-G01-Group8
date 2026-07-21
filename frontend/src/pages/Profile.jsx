import React, { useState, useEffect, useRef } from 'react';
import { Save, AlertTriangle, ArrowLeft, Camera } from 'lucide-react';
import DashboardLayout from '../components/layout/DashboardLayout';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../context/ToastContext';
import { useConfirm } from '../context/ConfirmationContext';
import '../styles/dashboard.scss';
import '../styles/profile.scss';

const Profile = () => {
  const { user, logout, token, updateUser } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const { showToast } = useToast();
  const { confirm } = useConfirm();
  
  // State for Form
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' });
  
  // State for Avatar Handling
  const [avatarUrl, setAvatarUrl] = useState(null);
  const [pendingFile, setPendingFile] = useState(null); 
  const [originalAvatar, setOriginalAvatar] = useState(null); 
  const [isSaved, setIsSaved] = useState(true); 

  const [loading, setLoading] = useState(false);

  // 1. Load User Data on Mount & React to Context Changes
  useEffect(() => {
    if (user) {
      // Sync Form Data
      setForm(prev => ({
        ...prev,
        firstName: user.firstName || '', 
        lastName: user.lastName || '',
        email: user.email || ''
      }));
      
      // Avatar Logic
      if (user.avatarUrl) {
        // Always sync local view to match Context (so previews appear here too)
        setAvatarUrl(user.avatarUrl);

        // --- CRITICAL FIX START ---
        // Only overwrite the BACKUP (originalAvatar) if we are in a "Saved" state.
        // If we are currently "Unsaved" (isSaved === false), it means the 'user' object 
        // changed because of a local preview update. We MUST NOT overwrite our backup 
        // with the preview image, or we lose the ability to revert!
        if (isSaved) {
          setOriginalAvatar(user.avatarUrl);
        }
        // --- CRITICAL FIX END ---
      }
    }
  }, [user, isSaved]); // Added isSaved to dependency array

  // 2. Handle File Selection (PREVIEW ONLY)
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    if (file.size > 2 * 1024 * 1024) { 
      showToast("Image size must be less than 2MB.", 'error');
      return; 
    }

    // Create a temporary local URL for preview
    const previewUrl = URL.createObjectURL(file);
    
    // Update Local State
    setAvatarUrl(previewUrl);
    setPendingFile(file);
    setIsSaved(false); // Mark as unsaved

    // Update Global Context IMMEDIATELY (Visuals only)
    // This triggers the useEffect above, but 'isSaved' is now false, so backup remains safe.
    updateUser({ avatarUrl: previewUrl });
  };

  const handleImageClick = () => fileInputRef.current.click();

  // 3. Handle "Save Changes" (Actual Upload + Text Update)
  const handleUpdate = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      let finalAvatarUrl = avatarUrl; // Default to current preview

      // Step A: Upload Image (if one is pending)
      if (pendingFile) {
        const formData = new FormData();
        formData.append('avatar', pendingFile);

        const imgRes = await fetch('http://localhost:8080/api/users/me/avatar', {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` },
          body: formData
        });

        if (!imgRes.ok) throw new Error("Failed to upload image");
        const imgData = await imgRes.json();
        finalAvatarUrl = imgData.data || imgData;
      }

      // Step B: Update Text Data
      const textRes = await fetch('http://localhost:8080/api/users/me', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ firstName: form.firstName, lastName: form.lastName, password: form.password || null })
      });

      if (!textRes.ok) throw new Error("Failed to update profile info");

      // Success!
      showToast("Profile saved successfully!", 'success');
      setForm(prev => ({ ...prev, password: '' }));
      setPendingFile(null); 
      
      // Update backup to the new, permanent image
      setIsSaved(true); 
      setOriginalAvatar(finalAvatarUrl); 

      // Final Context Update with REAL data
      updateUser({ 
        firstName: form.firstName, 
        lastName: form.lastName, 
        avatarUrl: finalAvatarUrl 
      });

    } catch (err) { 
      console.error(err); 
      showToast(err.message || "Update failed", 'error');
    } finally { 
      setLoading(false); 
    }
  };

  // 4. Handle Back Button (Intercept & Warn)
  const handleBack = async () => {
    if (!isSaved) {
        const discard = await confirm(
            "You have unsaved changes. If you leave now, your changes will be lost.",
            "Discard Changes?"
        );
        
        if (!discard) return; // User chose to stay

        // User chose to leave: REVERT Context immediately
        // originalAvatar is safe because of the fix in useEffect
        updateUser({ avatarUrl: originalAvatar });
    }
    navigate(-1);
  };

  const handleDelete = async () => {
    const isConfirmed = await confirm(
        "Deleting your account is permanent. All your data and history will be lost. Do you want to proceed?",
        "Delete Account"
    );

    if (isConfirmed) {
        try {
            const res = await fetch('http://localhost:8080/api/users/me', {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) { 
                logout(); 
            } else {
                showToast("Failed to delete account.", 'error');
            }
        } catch(e) { 
            showToast("Error deleting account", 'error');
        }
    }
  };

  if (!user) return null;

  return (
    <DashboardLayout>
      <div className="profile-container">
        <div className="profile-page-header">
          <button onClick={handleBack} className="btn-back" title="Go Back"><ArrowLeft size={24} /></button>
          <div className="header-content">
            <h2 className="section-title">Account Settings</h2>
            <p className="section-subtitle">Manage your personal details</p>
          </div>
        </div>
        
        <div className="profile-card">
          <div className="profile-user-info">
            <div className="avatar-wrapper" style={{ position: 'relative' }}>
              <div className="large-avatar" onClick={handleImageClick}
                style={{ 
                  cursor: 'pointer', 
                  backgroundImage: avatarUrl ? `url(${avatarUrl})` : 'none',
                  backgroundSize: 'cover', backgroundPosition: 'center',
                  border: '2px solid var(--border-light)', overflow: 'hidden', position: 'relative'
                }}
              >
                {!avatarUrl && (form.email ? form.email.charAt(0).toUpperCase() : 'U')}
                {/* Visual Overlay if Pending Save */}
                {!isSaved && pendingFile && (
                    <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, background: 'rgba(255, 193, 7, 0.9)', color: '#333', fontSize: '10px', textAlign: 'center', padding: '2px', fontWeight: 'bold' }}>
                        PREVIEW
                    </div>
                )}
              </div>
              <button className="camera-btn" onClick={handleImageClick} style={{ position: 'absolute', bottom: 0, right: -5, background: 'var(--maroon)', color: 'white', borderRadius: '50%', padding: '6px', border: '2px solid white', cursor: 'pointer' }}>
                <Camera size={16} />
              </button>
              <input type="file" ref={fileInputRef} onChange={handleFileChange} style={{ display: 'none' }} accept="image/*" />
            </div>
            <div className="user-details">
              <h3>{form.email}</h3>
              <p>{user?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Student'} Account</p>
              {!isSaved && <span style={{ fontSize: '12px', color: '#D97706', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px', marginTop: '4px' }}>
                <AlertTriangle size={12} /> Unsaved Changes
              </span>}
            </div>
          </div>

          <form onSubmit={handleUpdate}>
             <div className="form-grid">
              <div className="form-group">
                <label>First Name</label>
                <input className="form-input" value={form.firstName} onChange={(e) => { setForm({...form, firstName: e.target.value}); setIsSaved(false); }} required />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input className="form-input" value={form.lastName} onChange={(e) => { setForm({...form, lastName: e.target.value}); setIsSaved(false); }} required />
              </div>
              <div className="form-group full-width">
                <label>Email Address</label>
                <input className="form-input" value={form.email} disabled style={{ backgroundColor: '#F3F4F6' }} />
              </div>
              <div className="form-group full-width">
                <label>New Password</label>
                <input type="password" className="form-input" placeholder="Leave blank to keep current" value={form.password} onChange={(e) => { setForm({...form, password: e.target.value}); setIsSaved(false); }} />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn-save" disabled={loading || isSaved}>
                <Save size={18} /> 
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>

           <div className="danger-zone">
            <div className="danger-content">
              <div className="danger-icon-box"><AlertTriangle size={24} /></div>
              <div className="danger-text">
                <h4 className="danger-title">Delete Account</h4>
                <p className="danger-desc">Once you delete your account, there is no going back.</p>
                <button onClick={handleDelete} className="btn-delete">Delete My Account</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Profile;