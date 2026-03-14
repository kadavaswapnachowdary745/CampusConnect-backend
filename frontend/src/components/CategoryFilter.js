import React from 'react';

const categories = [
  { value: 'ALL', label: 'All Categories' },
  { value: 'BOOKS', label: 'Books' },
  { value: 'ELECTRONICS', label: 'Electronics' },
  { value: 'LAB_EQUIPMENT', label: 'Lab Equipment' },
  { value: 'HOSTEL_ESSENTIALS', label: 'Hostel Essentials' },
  { value: 'ACCESSORIES', label: 'Accessories' },
  { value: 'PROJECT_MATERIALS', label: 'Project Materials' },
  { value: 'OTHER', label: 'Other' }
];

const CategoryFilter = ({ value, onChange }) => (
  <div className="relative">
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className="appearance-none w-full px-4 py-3 bg-white border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent pr-10 cursor-pointer"
    >
      {categories.map((category) => (
        <option key={category.value} value={category.value}>
          {category.label}
        </option>
      ))}
    </select>
    <div className="absolute inset-y-0 right-0 flex items-center px-3 pointer-events-none">
      <svg className="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
      </svg>
    </div>
  </div>
);

export default CategoryFilter;
