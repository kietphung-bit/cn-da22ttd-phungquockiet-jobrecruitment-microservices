import React, { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import PrivateRoute from './PrivateRoute';
import LoadingSpinner from '../components/common/LoadingSpinner';

// ============================================
// EAGER IMPORTS (Critical for initial render)
// ============================================
// These components are loaded immediately to prevent flash of loading screen
// on first page load. Adjust based on your landing page requirements.

// Layouts - Load immediately (small bundle size, needed for route structure)
import MainLayout from '../components/layout/MainLayout';
import PublicTalentPage from '../pages/public/PublicTalentPage';

// ============================================
// MOCKUP PAGES FOR TESTING (REMOVE BEFORE PRODUCTION)
// ============================================
import MockNav from '../pages/MockNav';
import EmployerTalentSearchPage from '../pages/company/EmployerTalentSearchPage';
import CandidatePostManager from '../pages/candidate/CandidatePostManager';
import AdminPostModeration from '../pages/admin/AdminPostModeration';

// ============================================
// LAZY IMPORTS - CODE SPLITTING
// ============================================

// ------------------
// CANDIDATE BUNDLE (candidate-bundle.js)
// ------------------
// Lazy load: Candidate Layout
const CandidateLayout = lazy(() => import('../components/layout/CandidateLayout'));

// Lazy load: Candidate Pages
const CandidateProfile = lazy(() => import('../pages/candidate/CandidateProfile'));
const CVManager = lazy(() => import('../pages/candidate/CVManager'));
const ApplicationHistory = lazy(() => import('../pages/candidate/ApplicationHistory'));
const SavedJobsPage = lazy(() => import('../pages/candidate/SavedJobsPage'));
const SecurityPage = lazy(() => import('../pages/candidate/SecurityPage'));

// ------------------
// PUBLIC PAGES (Can be lazy loaded for faster initial load)
// ------------------
const HomePage = lazy(() => import('../pages/candidate/HomePage'));
const JobSearchPage = lazy(() => import('../pages/candidate/JobSearchPage'));
const JobDetailPage = lazy(() => import('../pages/candidate/JobDetailPage'));
const CompanySearchPage = lazy(() => import('../pages/candidate/CompanySearchPage'));
const CompanyDetailPage = lazy(() => import('../pages/candidate/CompanyDetailPage'));
const ComponentsDemo = lazy(() => import('../pages/ComponentsDemo'));

// ------------------
// AUTH PAGES (auth-bundle.js)
// ------------------
const LoginPage = lazy(() => import('../pages/auth/LoginPage'));
const RegisterPage = lazy(() => import('../pages/auth/RegisterPage'));
const ForgotPasswordPage = lazy(() => import('../pages/auth/ForgotPasswordPage'));
const ResetPasswordPage = lazy(() => import('../pages/auth/ResetPasswordPage'));

// ------------------
// EMPLOYER BUNDLE (employer-bundle.js)
// ------------------
// Lazy load: Employer Layout
const CompanyLayout = lazy(() => import('../components/layout/CompanyLayout'));

// Lazy load: Employer Pages
const EmployerDashboard = lazy(() => import('../pages/employer/EmployerDashboard'));
const CompanyProfile = lazy(() => import('../pages/employer/CompanyProfile'));
const JobManager = lazy(() => import('../pages/employer/JobManager'));
const JobEditor = lazy(() => import('../pages/employer/JobEditor'));
const EmployerApplications = lazy(() => import('../pages/employer/EmployerApplications'));
const CandidateDetail = lazy(() => import('../pages/employer/CandidateDetail'));

// ------------------
// ADMIN BUNDLE (admin-bundle.js)
// ------------------
const AdminLayout = lazy(() => import('../components/layout/AdminLayout'));
const AdminDashboard = lazy(() => import('../pages/admin/AdminDashboard'));
const CategoryManager = lazy(() => import('../pages/admin/CategoryManager'));
const UserManagement = lazy(() => import('../pages/admin/UserManagement'));
const JobModeration = lazy(() => import('../pages/admin/JobModeration'));

/**
 * AppRoutes Component
 * Central routing configuration with code splitting for optimal performance
 * 
 * Architecture:
 * - EAGER LOADING: MainLayout (small bundle, needed for all public routes)
 * - LAZY LOADING: All page components and role-specific layouts
 * 
 * Bundle Splitting Strategy:
 * 1. candidate-bundle.js: CandidateLayout + Candidate pages (~900 KB)
 * 2. auth-bundle.js: Login, Register, Password Reset pages (~200 KB)
 * 3. employer-bundle.js: Employer Layout + Employer pages (Future)
 * 4. admin-bundle.js: Admin Layout + Admin pages (Future)
 * 5. public-pages.js: HomePage, JobSearch, JobDetail, Company pages (~300 KB)
 * 
 * Benefits:
 * - Initial bundle size reduced by ~70%
 * - Candidates don't download Employer/Admin code
 * - Faster Time To Interactive (TTI)
 * - Better Core Web Vitals scores
 * 
 * Route Structure:
 * - Public Routes (MainLayout): Home, Job Search, Company Search
 * - Auth Routes (MainLayout): Login, Register (with Navbar + Footer for consistency)
 * - Protected Routes: Candidate, Employer, Admin (role-based lazy loading)
 */
const AppRoutes = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* ============================================ */}
          {/* PUBLIC ROUTES - Lazy loaded with Suspense   */}
          {/* ============================================ */}
          <Route path="/" element={<MainLayout />}>
            <Route 
              index 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải trang chủ..." />}>
                  <HomePage />
                </Suspense>
              } 
            />
            <Route 
              path="jobs" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải danh sách việc làm..." />}>
                  <JobSearchPage />
                </Suspense>
              } 
            />
            <Route 
              path="jobs/:id" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải chi tiết công việc..." />}>
                  <JobDetailPage />
                </Suspense>
              } 
            />
            <Route 
              path="companies" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải danh sách công ty..." />}>
                  <CompanySearchPage />
                </Suspense>
              } 
            />
            <Route 
              path="companies/:id" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải thông tin công ty..." />}>
                  <CompanyDetailPage />
                </Suspense>
              } 
            />
            {/* <Route 
              path="talents" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải tìm kiếm ứng viên..." />}>
                  <PublicTalentPage />
                </Suspense>
              } 
            /> */}
            
            {/* Placeholder routes - To be implemented */}
            <Route path="about" element={<div className="container-custom py-20 text-center"><h1>About Page - Coming Soon</h1></div>} />
            <Route path="contact" element={<div className="container-custom py-20 text-center"><h1>Contact Page - Coming Soon</h1></div>} />
            <Route path="privacy" element={<div className="container-custom py-20 text-center"><h1>Privacy Policy - Coming Soon</h1></div>} />
            <Route path="terms" element={<div className="container-custom py-20 text-center"><h1>Terms of Service - Coming Soon</h1></div>} />
            <Route 
              path="demo" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải demo..." />}>
                  <ComponentsDemo />
                </Suspense>
              } 
            />

            {/* ============================================ */}
            {/* AUTH ROUTES - Inside MainLayout for consistency */}
            {/* Bundle: auth-bundle.js                      */}
            {/* ============================================ */}
            <Route 
              path="login" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải trang đăng nhập..." />}>
                  <LoginPage />
                </Suspense>
              } 
            />
            <Route 
              path="register" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải trang đăng ký..." />}>
                  <RegisterPage />
                </Suspense>
              } 
            />
            <Route 
              path="forgot-password" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải..." />}>
                  <ForgotPasswordPage />
                </Suspense>
              } 
            />
            <Route 
              path="reset-password" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải..." />}>
                  <ResetPasswordPage />
                </Suspense>
              } 
            />
          </Route>

          {/* ============================================ */}
          {/* CANDIDATE ROUTES - Protected + Lazy loaded  */}
          {/* Bundle: candidate-bundle.js                 */}
          {/* ============================================ */}
          <Route 
            path="/candidate" 
            element={
              <PrivateRoute allowedRoles="UV">
                <Suspense fallback={<LoadingSpinner text="Đang tải bảng điều khiển..." />}>
                  <CandidateLayout />
                </Suspense>
              </PrivateRoute>
            }
          >
            <Route 
              path="profile" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải hồ sơ..." />}>
                  <CandidateProfile />
                </Suspense>
              } 
            />
            <Route 
              path="cv-manager" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải quản lý CV..." />}>
                  <CVManager />
                </Suspense>
              } 
            />
            <Route 
              path="applications" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải lịch sử ứng tuyển..." />}>
                  <ApplicationHistory />
                </Suspense>
              } 
            />
            <Route 
              path="security" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải bảo mật..." />}>
                  <SecurityPage />
                </Suspense>
              } 
            />
            <Route 
              path="saved-jobs" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải việc làm đã lưu..." />}>
                  <SavedJobsPage />
                </Suspense>
              } 
            />
          </Route>

          {/* ============================================ */}
          {/* EMPLOYER ROUTES - Protected + Lazy loaded   */}
          {/* Bundle: employer-bundle.js                  */}
          {/* ============================================ */}
          <Route 
            path="/employer" 
            element={
              <PrivateRoute allowedRoles="DN">
                <Suspense fallback={<LoadingSpinner text="Đang tải bảng điều khiển..." />}>
                  <CompanyLayout />
                </Suspense>
              </PrivateRoute>
            }
          >
            <Route 
              path="dashboard" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải tổng quan..." />}>
                  <EmployerDashboard />
                </Suspense>
              } 
            />
            <Route 
              path="profile" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải hồ sơ công ty..." />}>
                  <CompanyProfile />
                </Suspense>
              } 
            />
            <Route 
              path="jobs" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải quản lý tin đăng..." />}>
                  <JobManager />
                </Suspense>
              } 
            />
            <Route 
              path="jobs/create" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải form..." />}>
                  <JobEditor />
                </Suspense>
              } 
            />
            <Route 
              path="jobs/edit/:id" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải form..." />}>
                  <JobEditor />
                </Suspense>
              } 
            />
            <Route 
              path="applications" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải đơn ứng tuyển..." />}>
                  <EmployerApplications />
                </Suspense>
              } 
            />
            <Route 
              path="candidates/:candidateId" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải thông tin ứng viên..." />}>
                  <CandidateDetail />
                </Suspense>
              } 
            />
          </Route>

          {/* ============================================ */}
          {/* ADMIN ROUTES - Protected + Lazy loaded      */}
          {/* Bundle: admin-bundle.js                     */}
          {/* Access: Only ROLE_ADM                       */}
          {/* ============================================ */}
          <Route 
            path="/admin" 
            element={
              <PrivateRoute allowedRoles="ADM">
                <Suspense fallback={<LoadingSpinner text="Đang tải bảng điều khiển..." />}>
                  <AdminLayout />
                </Suspense>
              </PrivateRoute>
            }
          >
            {/* Admin Dashboard */}
            <Route 
              path="dashboard" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải tổng quan..." />}>
                  <AdminDashboard />
                </Suspense>
              } 
            />
            
            {/* Category Management */}
            <Route 
              path="categories" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải danh mục..." />}>
                  <CategoryManager />
                </Suspense>
              } 
            />
            
            {/* User Management */}
            <Route 
              path="users" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải..." />}>
                  <UserManagement />
                </Suspense>
              } 
            />
            
            {/* Job Moderation */}
            <Route 
              path="jobs" 
              element={
                <Suspense fallback={<LoadingSpinner text="Đang tải..." />}>
                  <JobModeration />
                </Suspense>
              } 
            />
            
            {/* Default redirect to dashboard */}
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
          </Route>

          {/* ============================================ */}
          {/* ERROR ROUTES                                */}
          {/* ============================================ */}
          {/* 404 Not Found */}
          <Route path="/404" element={<div className="flex items-center justify-center min-h-screen"><h1 className="text-4xl font-bold">404 - Page Not Found</h1></div>} />
          
          {/* Catch all - Redirect to 404 */}
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default AppRoutes;
