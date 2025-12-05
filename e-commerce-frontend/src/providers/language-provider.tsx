'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { en } from '../i18n/locales/en';
import { vi } from '../i18n/locales/vi';

type Language = 'en' | 'vi';
type Translations = typeof en;

interface LanguageContextType {
  language: Language;
  t: Translations;
  setLanguage: (lang: Language) => void;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

const STORAGE_KEY = 'ecommerce_lang_v2';

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  // Default to 'vi' for SSR consistency
  const [language, setLanguageState] = useState<Language>('vi');
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    try {
      const savedLang = localStorage.getItem(STORAGE_KEY);
      if (savedLang === 'en' || savedLang === 'vi') {
        setLanguageState(savedLang);
      }
    } catch (e) {
      console.error("Failed to read language from storage", e);
    } finally {
      setIsLoaded(true);
    }
  }, []);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    try {
        localStorage.setItem(STORAGE_KEY, lang);
    } catch (e) {
        console.error("Failed to save language to storage", e);
    }
  };

  const t = language === 'vi' ? vi : en;

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
