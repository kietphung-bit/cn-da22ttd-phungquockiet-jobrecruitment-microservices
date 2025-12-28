import React from 'react';
import JobCard from '../components/common/JobCard';
import CompanyCard from '../components/common/CompanyCard';

/**
 * ComponentsDemo Page
 * Showcases all reusable UI components with dummy data
 */
const ComponentsDemo = () => {
  // Dummy data for JobCard (matching actual database schema)
  const sampleJobs = [
    {
      jobId: 1,
      title: 'Senior Frontend Developer',
      company: 'TechCorp Inc.',
      companyLogo: null,
      salary: '25.000.000 ₫',
      location: 'Hà Nội',
    },
    {
      jobId: 2,
      title: 'Product Manager',
      company: 'Innovation Labs',
      companyLogo: null,
      salary: '30.000.000 ₫',
      location: 'Hồ Chí Minh',
    },
    {
      jobId: 3,
      title: 'UX/UI Designer',
      company: 'Creative Studio',
      companyLogo: null,
      salary: '20.000.000 ₫',
      location: 'Remote',
    },
  ];

  // Dummy data for CompanyCard
  const sampleCompanies = [
    {
      companyId: 1,
      name: 'TechCorp Inc.',
      logo: null,
      jobCount: 15,
    },
    {
      companyId: 2,
      name: 'Innovation Labs',
      logo: null,
      jobCount: 8,
    },
    {
      companyId: 3,
      name: 'Creative Studio',
      logo: null,
      jobCount: 12,
    },
    {
      companyId: 4,
      name: 'Digital Solutions',
      logo: null,
      jobCount: 20,
    },
  ];

  return (
    <div className="min-h-screen bg-neutral-50 py-12">
      <div className="container-custom">
        {/* Header */}
        <div className="mb-12 text-center">
          <h1 className="text-4xl font-bold text-neutral-900 mb-4">
            Component Showcase
          </h1>
          <p className="text-neutral-600">
            Preview of reusable UI components with TailwindCSS and lucide-react icons
          </p>
        </div>

        {/* JobCard Section */}
        <section className="mb-16">
          <h2 className="text-2xl font-bold text-neutral-900 mb-6">
            JobCard Component
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {sampleJobs.map((job) => (
              <JobCard key={job.jobId} job={job} />
            ))}
          </div>
        </section>

        {/* CompanyCard Section */}
        <section className="mb-16">
          <h2 className="text-2xl font-bold text-neutral-900 mb-6">
            CompanyCard Component
          </h2>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {sampleCompanies.map((company) => (
              <CompanyCard key={company.companyId} company={company} />
            ))}
          </div>
        </section>

        {/* Navbar & Footer Info */}
        <section className="bg-white rounded-lg shadow-md p-8">
          <h2 className="text-2xl font-bold text-neutral-900 mb-4">
            Layout Components
          </h2>
          <div className="space-y-4 text-neutral-700">
            <div>
              <h3 className="font-semibold text-lg mb-2">Navbar</h3>
              <ul className="list-disc list-inside space-y-1 text-sm">
                <li>Left: Circle logo with "JR" initials</li>
                <li>Center: Navigation links (Home, Jobs, Companies, Contact)</li>
                <li>Right: Search input with icon, Register (outline), Login (solid blue)</li>
                <li>Responsive: Hamburger menu for mobile devices</li>
                <li>Icons: lucide-react (Search, Menu, X)</li>
              </ul>
            </div>
            <div>
              <h3 className="font-semibold text-lg mb-2">Footer</h3>
              <ul className="list-disc list-inside space-y-1 text-sm">
                <li>4 Columns: Website Info/Logo, Contact Links, For Candidates, For Employers</li>
                <li>Social media icons: Facebook, Twitter, LinkedIn</li>
                <li>Contact info: Email, Phone, Address</li>
                <li>Bottom: Copyright text and policy links</li>
                <li>Responsive: Stacks to single column on mobile</li>
              </ul>
            </div>
          </div>
        </section>

        {/* Usage Instructions */}
        <section className="mt-12 bg-primary-50 rounded-lg p-8">
          <h2 className="text-2xl font-bold text-primary-900 mb-4">
            Usage Instructions
          </h2>
          <div className="space-y-4 text-neutral-700">
            <div>
              <h3 className="font-semibold mb-2">JobCard</h3>
              <pre className="bg-white p-4 rounded text-sm overflow-x-auto">
{`import JobCard from './components/common/JobCard';

const job = {
  jobId: 1,
  title: 'Job Title',
  company: 'Company Name',
  companyLogo: null, // or image URL
  salary: 'Salary (formatted)',
  location: 'Location'
};

<JobCard job={job} />`}
              </pre>
            </div>
            <div>
              <h3 className="font-semibold mb-2">CompanyCard</h3>
              <pre className="bg-white p-4 rounded text-sm overflow-x-auto">
{`import CompanyCard from './components/common/CompanyCard';

const company = {
  companyId: 1,
  name: 'Company Name',
  logo: null, // or image URL
  jobCount: 10
};

<CompanyCard company={company} />`}
              </pre>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default ComponentsDemo;
