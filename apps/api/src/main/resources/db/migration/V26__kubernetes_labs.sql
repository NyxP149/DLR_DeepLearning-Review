-- Parcours Docker et CI/CD étendu à Kubernetes : cinq laboratoires (objets, déploiements, configuration,
-- projet de déploiement et défi de diagnostic). Le contenu pédagogique vit dans content/devops/*.json ;
-- cette migration ne déclare que les lignes de suivi.
insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('DEVOPS-09', 9, 'kubernetes-pods-manifestes-et-objets', 'Kubernetes : pods, manifestes et objets', 'INTERMEDIAIRE', 70, 'LAB'),
    ('DEVOPS-10', 10, 'kubernetes-deployments-services-et-mises-a-jour', 'Deployments, Services et mises à jour progressives', 'INTERMEDIAIRE', 70, 'LAB'),
    ('DEVOPS-11', 11, 'kubernetes-configuration-secrets-sondes-et-ressources', 'Configuration, secrets, sondes et ressources', 'PROFESSIONNEL', 75, 'LAB'),
    ('DEVOPS-12', 12, 'projet-deployer-une-application-sur-kubernetes', 'Projet professionnel : déployer une application sur Kubernetes', 'PROJET', 80, 'PROJECT'),
    ('DEVOPS-13', 13, 'defi-kubernetes-diagnostiquer-un-deploiement-en-panne', 'Défi : diagnostiquer un déploiement Kubernetes en panne', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'DEVOPS'
  and not exists (select 1 from lab existing where existing.id = new_lab.id);
