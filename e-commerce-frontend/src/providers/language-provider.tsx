'use client';

import React, { createContext, useContext, useEffect } from 'react';
import { vi } from '../i18n/locales/vi';

type Language = 'vi'; // Restricted to 'vi'
type Translations = typeof vi;

interface LanguageContextType {
  language: Language;
  t: Translations;
  setLanguage: (lang: string) => void;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

type LanguageProviderProps = {
  children: React.ReactNode;
  initialLanguage?: 'en' | 'vi'; // accepted but we currently only support vi
};

export function LanguageProvider({ children}: LanguageProviderProps) {
  // Always Vietnamese
  const language: Language = 'vi';
  const t = vi;

  useEffect(() => {
    // Force document lang attribute
    if (typeof document !== 'undefined') {
      document.documentElement.lang = 'vi';
    }
  }, []);

  const setLanguage = () => {
    // No-op: Language is fixed to Vietnamese
    console.warn("Language switching is disabled. Defaulting to Vietnamese.");
  };

  return (
    <LanguageContext.Provider value={{ language, t, setLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useTranslation() {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error('useTranslation must be used within a LanguageProvider');
  }
  return context;
}
