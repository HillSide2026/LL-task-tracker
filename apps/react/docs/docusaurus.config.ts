import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const config: Config = {
  title: "Levine LLP Matter Platform",
  tagline: "Internal matter operations documentation",
  url: "https://matters.levinellp.ca",
  baseUrl: "/",
  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: "HillSide2026", // Usually your GitHub org/user name.
  projectName: "LL-task-tracker", // Usually your repo name.

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  presets: [
    [
      "classic",
      {
        docs: {
          path: "docs-live",
          routeBasePath: "docs",
          sidebarPath: require.resolve("./sidebars.ts"),
          breadcrumbs: false,
          editUrl: "https://github.com/HillSide2026/LL-task-tracker/tree/main/apps/react/docs/",
        },
        theme: {
          customCss: "./src/css/custom.css",
        },
      } satisfies Preset.Options,
    ],
  ],

  plugins: [],

  themeConfig: {
    // Replace with your project's social card
    image: "img/docusaurus-social-card.jpg",
    navbar: {
      title: "",
      logo: {
        alt: "Levine LLP Matter Platform Logo",
        src: "img/logo.svg",
      },
      items: [
        {
          href: "https://levinellp.ca",
          label: "Levine LLP",
          position: "left",
        },
        {
          type: "doc",
          docId: "intro",
          position: "left",
          label: "Docs",
        },
        {
          type: "doc",
          docId: "deployment",
          position: "left",
          label: "Deployment",
        },
        {
          href: "https://matters.levinellp.ca",
          label: "Matter Portal",
          position: "right",
        },
        {
          href: "https://github.com/HillSide2026/LL-task-tracker",
          label: "GitHub",
          position: "right",
        },
      ],
    },
    footer: {
      // style: "dark",
      links: [
        // {
        //   title: "Docs",
        //   items: [
        //     {
        //       label: "Tutorial",
        //       to: "/docs/intro",
        //     },
        //   ],
        // },
        // {
        //   title: "Community",
        //   items: [
        //     {
        //       label: "Stack Overflow",
        //       href: "https://stackoverflow.com/questions/tagged/docusaurus",
        //     },
        //     {
        //       label: "Discord",
        //       href: "https://discordapp.com/invite/docusaurus",
        //     },
        //     {
        //       label: "Twitter",
        //       href: "https://twitter.com/docusaurus",
        //     },
        //   ],
        // },
        // {
        //   title: "More",
        //   items: [
        //     {
        //       label: "Blog",
        //       to: "/blog",
        //     },
        //     {
        //       label: "GitHub",
        //       href: "https://github.com/facebook/docusaurus",
        //     },
        //   ],
        // },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Levine LLP.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  } satisfies Preset.ThemeConfig,
};

if (process.env.PROD === "true") {
  config.plugins.push([
    "@docusaurus/plugin-google-gtag",
    {
      trackingID: process.env.GTAG_ID,
      anonymizeIP: true,
    },
  ]);
}

export default config;
