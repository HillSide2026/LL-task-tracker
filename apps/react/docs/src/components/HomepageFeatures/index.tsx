import clsx from "clsx";
import Heading from "@theme/Heading";
import styles from "./styles.module.css";

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<"svg">>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: "Matter Opening",
    Svg: require("@site/static/img/chamaleon.svg").default,
    description: (
      <>
        Intake, engagement readiness, conflicts checks, and responsible lawyer
        assignment.
      </>
    ),
  },
  {
    title: "Lawyer Review",
    Svg: require("@site/static/img/camunda-logo-dark.svg").default,
    description: (
      <>
        Structured approval and return-for-fixes steps before a matter moves
        into active work.
      </>
    ),
  },
  {
    title: "Matter Maintenance",
    Svg: require("@site/static/img/padlock.svg").default,
    description: (
      <>
        Client waits, external waits, lawyer follow-ups, health flags, and
        operational exception queues.
      </>
    ),
  },
];

function Feature({ title, Svg, description }: FeatureItem) {
  return (
    <div className={clsx("col col--4")}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
