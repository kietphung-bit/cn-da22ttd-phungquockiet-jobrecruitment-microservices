# Job Recruitment Platform - Frontend

React + Vite + TailwindCSS frontend application for the Job Recruitment Platform.

## 🚀 Tech Stack

- **React 18** - UI Library
- **Vite** - Build Tool & Dev Server
- **TailwindCSS** - Utility-First CSS Framework
- **React Router DOM** - Client-side Routing
- **Axios** - HTTP Client
- **React Toastify** - Toast Notifications
- **Date-fns** - Date Manipulation

## 📁 Project Structure

```
src/
├── assets/                  # Static Resources
│   ├── images/             # Image files
│   └── styles/             # Global CSS (TailwindCSS)
├── components/             # Reusable UI Components
│   ├── common/            # Basic UI (Button, Input, Modal, etc.)
│   ├── layout/            # Page Layouts
│   │   ├── MainLayout.jsx    # Public pages layout (Navbar + Footer)
│   │   ├── AdminLayout.jsx   # Admin dashboard layout
│   │   └── CompanyLayout.jsx # Employer dashboard layout
│   └── features/          # Business Logic Components
│       ├── job/          # Job-related components (JobCard, JobFilter)
│       ├── cv/           # CV upload/preview components
│       └── auth/         # Authentication forms
├── configs/               # Configuration files
├── contexts/              # React Context (Global State)
├── hooks/                 # Custom React Hooks
├── pages/                 # Page Components
│   ├── auth/             # Login, Register
│   ├── candidate/        # Home, JobSearch, Profile
│   ├── employer/         # Dashboard, PostJob, ManageCandidates
│   ├── admin/            # UserManagement, JobModeration
│   └── error/            # 404, 403, 500
├── routes/                # Routing Configuration
│   ├── AppRoutes.jsx     # Main routing file
│   └── PrivateRoute.jsx  # Protected route HOC
├── services/              # API Service Layer
│   ├── auth.service.js
│   ├── job.service.js
│   └── user.service.js
└── utils/                 # Utility Functions
    ├── constants.js      # App constants
    ├── formatters.js     # Date/currency formatters
    └── validators.js     # Form validators
```

## 🎨 Design System

### Color Palette

```javascript
// Primary Blue
primary: '#4F46E5'
primary-50: '#EEF2FF'
primary-100: '#E0E7FF'
primary-500: '#4F46E5' (default)
primary-600: '#4338CA'
primary-700: '#3730A3'

// Neutral Grays
neutral-50: '#F9FAFB'
neutral-100: '#F3F4F6'
neutral-200: '#E5E7EB'
neutral-700: '#374151'
neutral-900: '#111827'
```

### TailwindCSS Utility Classes

```css
/* Buttons */
.btn - Base button styles
.btn-primary - Primary blue button
.btn-secondary - Gray button
.btn-outline - Outlined button

/* Inputs */
.input - Standard form input

/* Cards */
.card - Card container with shadow

/* Container */
.container-custom - Max-width container with padding
```

## 🛠️ Setup Instructions

### Prerequisites

- Node.js 18+ and npm
- Git

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd JobRecruitment/frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Start development server**
```bash
npm run dev
```

The application will open at `http://localhost:3000`

### Available Scripts

```bash
npm run dev       # Start development server
npm run build     # Build for production
npm run preview   # Preview production build
npm run lint      # Run ESLint (if configured)
```

## 📱 Implemented Pages

### Public Pages (MainLayout)

✅ **HomePage** (`/`)
- Hero section with search bar
- Featured companies showcase
- Hot jobs grid
- Job categories

✅ **JobSearchPage** (`/jobs`)
- Advanced search & filters
- Job listing with pagination
- Sidebar filters (desktop)
- Sort by newest/salary

✅ **JobDetailPage** (`/jobs/:id`)
- Full job description
- Requirements & benefits
- Company information sidebar
- Related jobs section
- Apply & Save buttons

### Placeholder Pages (To be implemented)

🔲 **Auth Pages**
- `/login` - Login page
- `/register` - Registration page

🔲 **Candidate Dashboard**
- `/profile` - User profile
- `/applied-jobs` - Applied jobs list
- `/saved-jobs` - Saved jobs list

🔲 **Employer Dashboard**
- `/employer/dashboard` - Employer dashboard
- `/employer/jobs` - Manage job postings
- `/employer/candidates` - View applicants

🔲 **Admin Dashboard**
- `/admin/dashboard` - Admin overview
- `/admin/users` - User management
- `/admin/jobs` - Job moderation

## 🔐 Authentication (To be implemented)

The project is structured to support JWT-based authentication:

1. **AuthContext** - Global authentication state
2. **PrivateRoute** - Protected route wrapper
3. **Auth Service** - Login/Register API calls
4. **Token Management** - Store JWT in localStorage

## 🌐 API Integration (To be implemented)

### Base Configuration

```javascript
// src/configs/axios.config.js
import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT token to requests
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default axiosInstance;
```

### Service Layer Example

```javascript
// src/services/job.service.js
import axiosInstance from '../configs/axios.config';

export const jobService = {
  getAllJobs: (params) => axiosInstance.get('/jobs', { params }),
  getJobById: (id) => axiosInstance.get(`/jobs/${id}`),
  searchJobs: (query) => axiosInstance.get(`/jobs/search?q=${query}`),
};
```

## 📦 Deployment

### Build for Production

```bash
npm run build
```

This creates an optimized build in the `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

### Deploy to Nginx (Production)

1. Build the application:
```bash
npm run build
```

2. Copy `dist/` folder to Nginx server:
```bash
scp -r dist/* user@server:/var/www/job-recruitment
```

3. Configure Nginx:
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /var/www/job-recruitment;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

## 🐳 Docker Support

### Dockerfile

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Build & Run

```bash
docker build -t job-recruitment-frontend .
docker run -p 3000:80 job-recruitment-frontend
```

## 🧪 Testing (To be implemented)

```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom
npm run test
```

## 📝 Next Steps

1. **Implement Authentication**
   - Login/Register pages
   - AuthContext for state management
   - PrivateRoute component
   - Token refresh logic

2. **Build Candidate Dashboard**
   - Profile management
   - CV upload/management
   - Job application flow
   - Saved jobs

3. **Build Employer Dashboard**
   - Company profile
   - Post job form
   - Manage applications
   - Candidate search

4. **Build Admin Dashboard**
   - User management
   - Job moderation
   - Analytics dashboard
   - System settings

5. **API Integration**
   - Connect all services to backend
   - Error handling
   - Loading states
   - Toast notifications

6. **Optimization**
   - Code splitting
   - Lazy loading
   - Image optimization
   - Performance monitoring

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

---

**Built with ❤️ by Job Recruitment Team**
